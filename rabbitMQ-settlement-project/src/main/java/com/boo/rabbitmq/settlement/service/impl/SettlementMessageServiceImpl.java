package com.boo.rabbitmq.settlement.service.impl;

import com.boo.moodymq.listener.AbstractMessageListener;
import com.boo.moodymq.sender.TransMessageSender;
import com.boo.rabbitmq.settlement.dto.OrderMessageDTO;
import com.boo.rabbitmq.settlement.entity.Settlement;
import com.boo.rabbitmq.settlement.enums.RabbitComponentEnum;
import com.boo.rabbitmq.settlement.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.rabbitmq.settlement.enums.RabbitComponentEnum.RoutingKey;
import com.boo.rabbitmq.settlement.enums.SettlementStatusEnum;
import com.boo.rabbitmq.settlement.service.SettlementMessageService;
import com.boo.rabbitmq.settlement.service.SettlementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 结算消息服务impl
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Slf4j
@Service
public class SettlementMessageServiceImpl extends AbstractMessageListener {

  @Autowired SettlementService settlementService;
  ObjectMapper objectMapper = new ObjectMapper();
  @Autowired TransMessageSender sender;
  public static final String SETTLEMENT_EXCHANGE = ExchangeEnum.SETTLEMENT_ORDER.getCode();
  public static final String ROUTING_KEY_ORDER = RoutingKey.KEY_ORDER.getCode();
  /** 处理消息 */
  @Override
  public void receiveMessage(@Payload Message message) {

    final String msg = new String(message.getBody());
    try {
      OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
      log.info("收到订单结算信息 :orderSettlementDTO:{}", orderMessageDTO);
      Settlement settlement = new Settlement();
      settlement.setAmount(orderMessageDTO.getPrice());
      settlement.setDate(LocalDateTime.now());
      settlement.setOrderId(orderMessageDTO.getOrderId());
      settlement.setStatus(SettlementStatusEnum.SUCCESS);
      settlement.setTransactionId(
          settlementService.settlement(orderMessageDTO.getAccountId(), orderMessageDTO.getPrice()));
      settlementService.save(settlement);
      orderMessageDTO.setSettlementId(settlement.getId());
      log.info("订单结算完毕，发回订单系统:settlementOrderDTO:{}", orderMessageDTO);
      sender.send(SETTLEMENT_EXCHANGE, ROUTING_KEY_ORDER, orderMessageDTO);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
