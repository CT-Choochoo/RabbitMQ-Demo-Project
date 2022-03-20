package com.boo.rabbitmq.reward.service.impl;

import com.boo.rabbitmq.reward.dto.OrderMessageDTO;
import com.boo.rabbitmq.reward.entity.Reward;
import com.boo.rabbitmq.reward.enums.RewardStatusEnum;
import com.boo.rabbitmq.reward.service.RewardMessageService;
import com.boo.rabbitmq.reward.service.RewardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 奖励消息服务impl
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Service
@Slf4j
public class RewardMessageServiceImpl implements RewardMessageService {

  @Autowired @Lazy ConnectionFactory connectionFactory;
  ObjectMapper objectMapper = new ObjectMapper();
  @Autowired RewardService rewardService;

  final String exchangeName = "exchange.order.reward";
  final String queueName = "queue.reward";
  final String routingKey = "key.reward";
  /** 处理消息 声明交换机 ， 队列 ，绑定关系，定义消费方式 */
  @Override
  @Async
  public void handleMessage() throws InterruptedException {
    log.info("start listening message");
    try (Connection connection = connectionFactory.newConnection()) {

      final Channel channel = connection.createChannel();
      channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, true, false, null);
      channel.queueDeclare(queueName, true, false, false, null);
      channel.queueBind(queueName, exchangeName, routingKey);

      channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
      while (true) {
        Thread.sleep(10000000);
      }
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
    }
  }

  /** 提供回调 业务流程，组装产生信息，发送给queue.order */
  DeliverCallback deliverCallback =
      (consumerTag, message) -> {
        try (Connection connection = connectionFactory.newConnection()) {

          final String msg = new String(message.getBody());
          log.info("deliverCallback:messageBody:{}", msg);

          final OrderMessageDTO orderMessageDTO =
              objectMapper.readValue(msg, OrderMessageDTO.class);
          Reward reward = new Reward();
          reward.setOrderId(orderMessageDTO.getOrderId());
          reward.setStatus(RewardStatusEnum.SUCCESS);
          reward.setAmount(orderMessageDTO.getPrice());
          reward.setDate(LocalDateTime.now());
          rewardService.save(reward);
          orderMessageDTO.setRewardId(reward.getId());
          log.info("handleOrderService:settlementOrderDTO:{}", orderMessageDTO);

          final String exchangeName = "exchange.order.reward";
          final Channel channel = connection.createChannel();

          channel.basicPublish(
              exchangeName, "key.order", null, objectMapper.writeValueAsBytes(orderMessageDTO));

        } catch (TimeoutException e) {
          e.printStackTrace();
        }
      };
}
