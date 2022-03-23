package com.boo.order.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.order.manager.convert.OrderDetailConvert;
import com.boo.order.manager.dao.OrderDetailMapper;
import com.boo.order.manager.dto.OrderMessageDTO;
import com.boo.order.manager.enums.OrderStatusEnum;
import com.boo.order.manager.po.OrderDetail;
import com.boo.order.manager.service.OrderDetailService;
import com.boo.order.manager.vo.OrderCreateVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 订单逻辑处理
 *
 * @author gaobo
 * @date 2022/03/18
 */
@Slf4j
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>
    implements OrderDetailService {

  ObjectMapper objectMapper = new ObjectMapper();

  @Autowired OrderDetailMapper mapper;
  @Autowired OrderDetailConvert orderDetailConvert;
  @Autowired @Lazy ConnectionFactory connectionFactory;
  @Autowired @Lazy RabbitTemplate rabbitTemplate;

  /**
   * 创建订单
   *
   * @param orderCreateVO 订单创建签证官
   * @throws IOException IO Exception
   */
  @Override
  public void createOrder(OrderCreateVO orderCreateVO) throws IOException {
    //  1.收到订单，更新状态和时间并保存
    OrderDetail orderDetail = orderDetailConvert.valueObject2Entity(orderCreateVO);
    orderDetail.setStatus(OrderStatusEnum.ORDER_CREATING);
    orderDetail.setDate(LocalDateTime.now());
    mapper.insert(orderDetail);
    //  2.构建dto对象发送消息
    OrderMessageDTO orderMessageDTO = orderDetailConvert.entity2DataTransferObject(orderDetail);
    orderMessageDTO.setOrderStatus(OrderStatusEnum.ORDER_CREATING);
    String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
    final CorrelationData correlationData = new CorrelationData();
    correlationData.setId(orderDetail.getId().toString());
    //  5.发送消息给商家队列
    rabbitTemplate.convertAndSend(
        "exchange.order.restaurant", "key.restaurant", orderMessageDTO.toString(),correlationData);
  }

  /**
   * 创建订单列表
   *
   * @param list 列表
   */
  @Override
  public void createOrderList(List<OrderCreateVO> list)
      throws IOException, TimeoutException, InterruptedException {

    final List<OrderDetail> collect =
        list.stream()
            .map(
                item -> {
                  OrderDetail orderDetail = orderDetailConvert.valueObject2Entity(item);
                  orderDetail.setStatus(OrderStatusEnum.ORDER_CREATING);
                  orderDetail.setDate(LocalDateTime.now());
                  return orderDetail;
                })
            .collect(Collectors.toList());

    //  1.收到订单，更新状态和时间并保存

    this.saveBatch(collect);

    //  2.获取connection

    //  3.获取channel
    try (Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false)) {
      //    设置异常投递返回
      this.settingCallBackReturnListener(channel);

      //  4.构建dto对象发送消息
      final List<OrderMessageDTO> dtoList =
          collect.stream()
              .map(
                  item -> {
                    OrderMessageDTO orderMessageDTO =
                        orderDetailConvert.entity2DataTransferObject(item);
                    orderMessageDTO.setOrderStatus(OrderStatusEnum.ORDER_CREATING);
                    return orderMessageDTO;
                  })
              .collect(Collectors.toList());
      final ArrayList<String> arrayDto = new ArrayList<>(50);
      for (OrderMessageDTO dto : dtoList) {
        arrayDto.add(objectMapper.writeValueAsString(dto));
      }
      //  5.发送消息给商家队列
      for (String str : arrayDto) {
        this.sendSingleMessageConfirm(
            channel, "exchange.order.restaurant", "key.restaurant", str.getBytes());
      }
    }
  }

  /**
   * 发送单一消息确认(推荐使用)
   *
   * @param exchangeName 交换名称
   * @param routingKey 路由关键
   * @param msg 味精
   */
  private void sendSingleMessageConfirm(
      Channel channel, String exchangeName, String routingKey, byte[] msg)
      throws IOException, InterruptedException {
    //      标记发送确认
    channel.confirmSelect();
    log.info("发送给商家队列消息：[{}]", new String(msg));
    //    设置消息超时时间
    //    final BasicProperties props = new Builder().expiration("15000").build();
    channel.basicPublish(exchangeName, routingKey, null, msg);
    log.info("message sent");
    //      等待消息发送成功
    if (channel.waitForConfirms()) {
      log.info("RabbitMQ confirm success!");
    } else {
      log.info("RabbitMQ confirm failed!");
    }
  }

  /**
   * 发送多个消息确认
   *
   * @param channel 通道
   * @param exchangeName 交换机名称
   * @param routingKey 路由键
   * @param msg 消息
   * @throws IOException IO Exception
   * @throws InterruptedException 中断异常
   */
  private void sendMultipleMessageConfirm(
      Channel channel, String exchangeName, String routingKey, byte[] msg)
      throws IOException, InterruptedException {
    //      标记发送确认
    channel.confirmSelect();
    for (int i = 0; i < 9; i++) {
      channel.basicPublish(exchangeName, routingKey, null, msg);
      log.info("message sent");
    }
    //      等待消息发送成功
    if (channel.waitForConfirms()) {
      log.info("RabbitMQ confirm success!");
    } else {
      log.info("RabbitMQ confirm failed!");
    }
  }

  /**
   * 发送异步消息确认
   *
   * @param channel 通道
   * @param exchangeName 交换名称
   * @param routingKey 路由关键
   * @param msg 消息体
   */
  private void sendAsyncMessageConfirm(
      Channel channel, String exchangeName, String routingKey, byte[] msg)
      throws InterruptedException, IOException {
    channel.confirmSelect();

    //    创建confirm异步监听
    final ConfirmListener listener =
        new ConfirmListener() {
          @Override
          public void handleAck(long deliveryTag, boolean multiple) {
            log.info("调用成功！ deliveryTag:{},是否多条：{}", deliveryTag, multiple);
          }

          @Override
          public void handleNack(long deliveryTag, boolean multiple) {
            log.info("调用失败！ deliveryTag:{},是否多条：{}", deliveryTag, multiple);
          }
        };
    //  设置到channel
    channel.addConfirmListener(listener);

    for (int i = 0; i < 2; i++) {
      // mandatory 消息返回机制，保证消息正确投递到队列 此处故意写错routingKey
      //      channel.basicPublish(exchangeName, routingKey + 1, true, null, msg);
      channel.basicPublish(exchangeName, routingKey, true, null, msg);
      log.info("message sent");
    }
    Thread.sleep(3000);
  }

  /**
   * 设置回调返回侦听器
   *
   * @param channel 信道
   */
  private void settingCallBackReturnListener(Channel channel) {
    channel.addReturnListener(
        returnMessage -> {
          log.info("发送消息无法路由！Message Return：[{}]", returnMessage.toString());
          //              TODO 消息投递异常
        });
  }

  /**
   * @param channel 信道
   */
  private void settingNewListenerReturnListener(Channel channel) {
    channel.addReturnListener(
        (replyCode, replyText, exchange, routingKey1, properties, body) -> {
          log.info(
              "发送消息无法路由！Message Return：replyCode：{},replyText:{},exchange:{},routingKey:{},BasicProperties:{},body:{}",
              replyCode,
              replyText,
              exchange,
              routingKey1,
              properties,
              new String(body));
          //              TODO 消息投递异常
        });
  }
}
