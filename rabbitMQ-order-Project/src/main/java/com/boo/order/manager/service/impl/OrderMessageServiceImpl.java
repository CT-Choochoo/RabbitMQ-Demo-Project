package com.boo.order.manager.service.impl;

import com.boo.order.manager.dto.OrderMessageDTO;
import com.boo.order.manager.enums.OrderStatusEnum;
import com.boo.order.manager.po.OrderDetail;
import com.boo.order.manager.service.OrderDetailService;
import com.boo.order.manager.service.OrderMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderMessageServiceImpl implements OrderMessageService {

  ObjectMapper objectMapper = new ObjectMapper();

  @Autowired OrderDetailService orderDetailService;
  /**
   * 处理消息
   *
   * @throws IOException IO Exception
   * @throws TimeoutException 超时异常
   * @throws InterruptedException 中断异常
   */
  @Override
  @Async
  public void handleMessage() throws IOException, TimeoutException, InterruptedException {
    //    1. 创建连接
    log.info("start listening message");
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("localhost");
    connectionFactory.setUsername("admin");
    connectionFactory.setPassword("admin");
    try (Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel()) {

      // 声明队列
      channel.queueDeclare("queue.order", true, false, false, null);

      /*---------------------restaurant声明商家交换机 、binding key---------------------*/
      // 声明exchange
      channel.exchangeDeclare(
          "exchange.order.restaurant", BuiltinExchangeType.DIRECT, true, false, null);
      // 绑定交换机和队列
      channel.queueBind("queue.order", "exchange.order.restaurant", "key.order");

      /*---------------------deliveryman 声明骑手交换机 、binding key---------------------*/
      channel.exchangeDeclare(
          "exchange.order.deliveryman", BuiltinExchangeType.DIRECT, true, false, null);

      channel.queueBind("queue.order", "exchange.order.deliveryman", "key.order");

      /*---------------------settlement 声明结算交换机 、binding key（fanout可以不设置）---------------------*/

      channel.exchangeDeclare(
          "exchange.settlement.order", BuiltinExchangeType.FANOUT, true, false, null);

      channel.queueBind("queue.order", "exchange.settlement.order", "key.order");

      /*---------------------reward 声明积分交换机，routing key---------------------*/

      channel.exchangeDeclare(
          "exchange.order.reward", BuiltinExchangeType.TOPIC, true, false, null);

      channel.queueBind("queue.order", "exchange.order.reward", "key.order");
      // 生成一个服务器生成的 consumerTag
      channel.basicConsume("queue.order", true, deliverCallback, consumerTag -> {});
      while (true) {
        Thread.sleep(10000000);
      }
    }
  }

  /** 提供回调 */
  DeliverCallback deliverCallback =
      (consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
        try {
          //          获取队列中的对象,判断阶段做对应处处理
          OrderMessageDTO orderMessageDTO =
              objectMapper.readValue(messageBody, OrderMessageDTO.class);
          OrderDetail entity = orderDetailService.getById(orderMessageDTO.getOrderId());

          switch (entity.getStatus()) {
            case ORDER_CREATING:
              executeOrderCreating(orderMessageDTO, entity, connectionFactory);
              break;
            case RESTAURANT_CONFIRMED:
              executeRestaurantConfirmed(orderMessageDTO, entity, connectionFactory);
              break;
            case DELIVERYMAN_CONFIRMED:
              executeDeliverymanConfirmed(orderMessageDTO, entity, connectionFactory);
              break;
            case SETTLEMENT_CONFIRMED:
              if (null != orderMessageDTO.getRewardId()) {
                entity.setStatus(OrderStatusEnum.ORDER_CREATED);
                entity.setRewardId(orderMessageDTO.getRewardId());
              } else {
                entity.setStatus(OrderStatusEnum.FAILED);
              }
              orderDetailService.updateById(entity);
              break;
            default:
              break;
          }

        } catch (JsonProcessingException | TimeoutException e) {
          e.printStackTrace();
        }
      };

  /**
   * 送货人执行确认
   *
   * @param orderMessageDTO 订单消息dto
   * @param entity 实体
   * @param connectionFactory 连接工厂
   */
  private void executeDeliverymanConfirmed(
      OrderMessageDTO orderMessageDTO, OrderDetail entity, ConnectionFactory connectionFactory)
      throws IOException, TimeoutException {
    if (null != orderMessageDTO.getSettlementId()) {
      entity.setStatus(OrderStatusEnum.SETTLEMENT_CONFIRMED);
      entity.setSettlementId(orderMessageDTO.getSettlementId());
      orderDetailService.updateById(entity);
      try (Connection connection = connectionFactory.newConnection();
          Channel channel = connection.createChannel()) {
        String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
        channel.basicPublish("exchange.order.reward", "key.reward", null, messageToSend.getBytes());
      }
    } else {
      entity.setStatus(OrderStatusEnum.FAILED);
      orderDetailService.updateById(entity);
    }
  }

  /**
   * 执行餐厅确认步骤
   *
   * @param orderMessageDTO 订单消息dto
   * @param entity 实体
   * @param connectionFactory 连接工厂
   * @throws IOException ioexception
   * @throws TimeoutException 超时异常
   */
  private void executeRestaurantConfirmed(
      OrderMessageDTO orderMessageDTO, OrderDetail entity, ConnectionFactory connectionFactory)
      throws IOException, TimeoutException {

    if (null != orderMessageDTO.getDeliverymanId()) {
      entity.setStatus(OrderStatusEnum.DELIVERYMAN_CONFIRMED);
      entity.setDeliverymanId(orderMessageDTO.getDeliverymanId());
      orderDetailService.updateById(entity);
      try (Connection connection = connectionFactory.newConnection();
          Channel channel = connection.createChannel()) {
        String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
        channel.basicPublish(
            "exchange.order.settlement", "key.settlement", null, messageToSend.getBytes());
      }
    } else {
      entity.setStatus(OrderStatusEnum.FAILED);
      orderDetailService.updateById(entity);
    }
  }

  /**
   * 执行订单创建
   *
   * <p>1. 更新数据库信息，a.订单信息状态变更 b.商品价格定义
   *
   * <p>2. 将DTO放入消息队列
   *
   * @param orderMessageDTO 订单消息dto
   * @param entity 实体
   * @param connectionFactory 连接工厂
   * @throws IOException IOexception
   * @throws TimeoutException 超时异常
   */
  private void executeOrderCreating(
      OrderMessageDTO orderMessageDTO, OrderDetail entity, ConnectionFactory connectionFactory)
      throws IOException, TimeoutException {

    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
      entity.setStatus(OrderStatusEnum.RESTAURANT_CONFIRMED);
      entity.setPrice(orderMessageDTO.getPrice());
      orderDetailService.updateById(entity);

      try (Connection connection = connectionFactory.newConnection();
          Channel channel = connection.createChannel()) {
        String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
        channel.basicPublish(
            "exchange.order.deliveryman", "key.deliveryman", null, messageToSend.getBytes());
      }
    } else {
      entity.setStatus(OrderStatusEnum.FAILED);
      orderDetailService.updateById(entity);
    }
  }
}
