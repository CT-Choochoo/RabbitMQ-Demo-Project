package com.boo.rabbitmq.settlement.service.impl;

import com.boo.rabbitmq.settlement.dto.OrderMessageDTO;
import com.boo.rabbitmq.settlement.entity.Settlement;
import com.boo.rabbitmq.settlement.enums.OrderStatusEnum;
import com.boo.rabbitmq.settlement.enums.SettlementStatusEnum;
import com.boo.rabbitmq.settlement.service.SettlementMessageService;
import com.boo.rabbitmq.settlement.service.SettlementService;
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
 * 结算消息服务impl
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Slf4j
@Service
public class SettlementMessageServiceImpl implements SettlementMessageService {

  @Autowired SettlementService settlementService;
  @Autowired @Lazy ConnectionFactory connectionFactory;
  ObjectMapper objectMapper = new ObjectMapper();
  /** 处理消息 */
  @Override
  @Async
  public void handleMessage() throws InterruptedException {

    final String exchangeName = "exchange.order.settlement";
    final String queueName = "queue.settlement";
    final String routingKey = "key.settlement";

    try (Connection connection = connectionFactory.newConnection()) {
      final Channel channel = connection.createChannel();
      channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true, false, null);
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

  /** 提供回调 收到消息与银行结算 ，发送消息到订单服务 */
  DeliverCallback deliverCallback =
      (consumerTag, message) -> {
        final String msg = new String(message.getBody());
        final OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
        log.info("handleOrderService:orderSettlementDTO:{}", orderMessageDTO);
        Settlement settlement = new Settlement();
        settlement.setAmount(orderMessageDTO.getPrice());
        settlement.setDate(LocalDateTime.now());
        settlement.setOrderId(orderMessageDTO.getOrderId());
        settlement.setStatus(SettlementStatusEnum.SUCCESS);
        settlement.setTransactionId(
            settlementService.settlement(
                orderMessageDTO.getAccountId(), orderMessageDTO.getPrice()));
        settlementService.save(settlement);
        orderMessageDTO.setSettlementId(settlement.getId());
        log.info("handleOrderService:settlementOrderDTO:{}", orderMessageDTO);

        try (Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel()) {
          String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
          channel.basicPublish(
              "exchange.settlement.order", "key.order", null, messageToSend.getBytes());
        } catch (TimeoutException e) {
          e.printStackTrace();
        }
      };
}
