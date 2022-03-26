package com.boo.rabbitmq.settlement.service.impl;

import com.boo.rabbitmq.settlement.dto.OrderMessageDTO;
import com.boo.rabbitmq.settlement.entity.Settlement;
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
public class SettlementMessageServiceImpl implements SettlementMessageService {

  @Autowired SettlementService settlementService;
  @Autowired RabbitTemplate rabbitTemplate;
  ObjectMapper objectMapper = new ObjectMapper();

  @Value(value = "${rabbitComponent.exchange.settlementOrder}")
  public String exchangeSettlement;

  @Value(value = "${rabbitComponent.routingKey.order}")
  public String routingKeyOrder;
  /** 处理消息 */
  @RabbitListener(
      bindings = {
        @QueueBinding(
            value =
                @Queue(
                    name = "${rabbitComponent.queue.settlement}",
                    arguments = {
                      @Argument(name = "x-message-ttl", value = "1000", type = "java.lang.Long"),
                      @Argument(
                          name = "x-dead-letter-exchange",
                          value = "${rabbitComponent.exchange.dlx}"),
                      @Argument(name = "x-max-length", value = "20", type = "java.lang.Long")
                    }),
            exchange =
                @Exchange(
                    name = "${rabbitComponent.exchange.settlement}",
                    type = ExchangeTypes.FANOUT),
            key = "${rabbitComponent.routingKey.settlement}")
      })
  @Override
  public void handleMessage(@Payload Message message) throws JsonProcessingException {

    final String msg = new String(message.getBody());
    final OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
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

    String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
    rabbitTemplate.send(
        exchangeSettlement,
        routingKeyOrder,
        new Message(messageToSend.getBytes()),
        new CorrelationData() {
          {
            setId(orderMessageDTO.getOrderId().toString());
          }
        });
  }
}
