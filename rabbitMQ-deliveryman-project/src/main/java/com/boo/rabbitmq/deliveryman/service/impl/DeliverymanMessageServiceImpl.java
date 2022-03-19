package com.boo.rabbitmq.deliveryman.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.boo.rabbitmq.deliveryman.dto.OrderMessageDTO;
import com.boo.rabbitmq.deliveryman.entity.Deliveryman;
import com.boo.rabbitmq.deliveryman.enums.DeliverymanStatusEnum;
import com.boo.rabbitmq.deliveryman.service.DeliverymanMessageService;
import com.boo.rabbitmq.deliveryman.service.DeliverymanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 送货人消息服务impl
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Slf4j
@Service
public class DeliverymanMessageServiceImpl implements DeliverymanMessageService {

  @Autowired ConnectionFactory factory;
  @Autowired DeliverymanService deliverymanService;

  ObjectMapper objectMapper = new ObjectMapper();
  /**
   * 处理消息 @throws IOException ioexception
   *
   * @throws TimeoutException 超时异常
   */
  @Override
  @Async
  public void handleMessage() throws IOException, TimeoutException {
    final Connection connection = factory.newConnection();
    final Channel channel = connection.createChannel();

    final String exchangeName = "exchange.order.deliveryman";
    final String queueName = "queue.deliveryman";
    final String routingKey = "key.deliveryman";
    //    声明骑手交换机
    channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true, false, null);
    //    声明骑手队列
    channel.queueDeclare(queueName, true, false, false, null);

    //    绑定交换机和队列
    channel.queueBind(queueName, exchangeName, routingKey);
    //    定义消息消费方法
    channel.basicConsume(queueName, true, deliverymanCallback, consumerTag -> {});
  }

  /** 送货人回调 处理本次自身操作，并发送给下一阶段消息 */
  DeliverCallback deliverymanCallback =
      (consumerTag, message) -> {
        final String msg = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", msg);
        final OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
        final List<Deliveryman> list =
            deliverymanService.list(
                new LambdaQueryWrapper<Deliveryman>()
                    .eq(Deliveryman::getStatus, DeliverymanStatusEnum.AVALIABLE));

        final Deliveryman deliveryman = list.get(0);
        orderMessageDTO.setDeliverymanId(deliveryman.getId());
        log.info("onMessage:restaurantOrderMessageDTO:{}", orderMessageDTO);

        try (Connection connection = factory.newConnection()) {
          final Channel channel = connection.createChannel();
          String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
          channel.basicPublish(
              "exchange.order.restaurant", "key.order", null, messageToSend.getBytes());
        } catch (TimeoutException e) {
          e.printStackTrace();
        }
      };
}
