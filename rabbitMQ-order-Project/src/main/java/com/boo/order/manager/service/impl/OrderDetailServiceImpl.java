package com.boo.order.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.moodymq.sender.TransMessageSender;
import com.boo.order.manager.convert.OrderDetailConvert;
import com.boo.order.manager.dao.OrderDetailMapper;
import com.boo.order.manager.dto.OrderMessageDTO;
import com.boo.order.manager.enums.OrderStatusEnum;
import com.boo.order.manager.enums.RabbitComponentEnum;
import com.boo.order.manager.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.order.manager.enums.RabbitComponentEnum.RoutingKey;
import com.boo.order.manager.po.OrderDetail;
import com.boo.order.manager.service.OrderDetailService;
import com.boo.order.manager.vo.OrderCreateVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  @Autowired TransMessageSender sender;

  public static String exchangeRestaurant = ExchangeEnum.ORDER_RESTAURANT.getCode();

  public static String routingKeyRestaurant = RoutingKey.KEY_RESTAURANT.getCode();

  /**
   * 创建订单
   *
   * @param orderCreateVO 订单创建签证官
   */
  @Override
  public void createOrder(OrderCreateVO orderCreateVO) {
    //  1.收到订单，更新状态和时间并保存
    OrderDetail orderDetail = orderDetailConvert.valueObject2Entity(orderCreateVO);
    orderDetail.setStatus(OrderStatusEnum.ORDER_CREATING);
    orderDetail.setDate(LocalDateTime.now());
    mapper.insert(orderDetail);
    //  2.构建dto对象发送消息
    OrderMessageDTO orderMessageDTO = orderDetailConvert.entity2DataTransferObject(orderDetail);
    orderMessageDTO.setOrderStatus(OrderStatusEnum.ORDER_CREATING);
    //  5.发送消息给商家队列
    sender.send(exchangeRestaurant, routingKeyRestaurant, orderMessageDTO);
  }

  /**
   * 创建订单列表
   *
   * @param list 列表
   */
  @Override
  public void createOrderList(List<OrderCreateVO> list) throws IOException {

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
    //  5.发送消息给商家队列
    for (OrderMessageDTO dto : dtoList) {
      sender.send(exchangeRestaurant, routingKeyRestaurant, dto);
      log.info("订单已发送 message sent");
    }
  }

  /**
   * 发送单一消息确认(推荐使用)
   *
   * @param exchangeName 交换名称
   * @param routingKey 路由关键
   */
  private void sendSingleMessageConfirm(String exchangeName, String routingKey, OrderMessageDTO dto)
      throws JsonProcessingException {

    //    设置发送消息附加信息

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
