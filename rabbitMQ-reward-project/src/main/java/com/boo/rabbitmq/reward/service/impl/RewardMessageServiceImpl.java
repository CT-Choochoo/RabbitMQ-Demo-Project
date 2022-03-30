package com.boo.rabbitmq.reward.service.impl;

import com.boo.moodymq.listener.AbstractMessageListener;
import com.boo.moodymq.sender.TransMessageSender;
import com.boo.rabbitmq.reward.dto.OrderMessageDTO;
import com.boo.rabbitmq.reward.entity.Reward;
import com.boo.rabbitmq.reward.enums.RabbitComponentEnum;
import com.boo.rabbitmq.reward.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.rabbitmq.reward.enums.RabbitComponentEnum.RoutingKey;
import com.boo.rabbitmq.reward.enums.RewardStatusEnum;
import com.boo.rabbitmq.reward.service.RewardService;
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
 * 奖励消息服务impl
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Service
@Slf4j
public class RewardMessageServiceImpl extends AbstractMessageListener {

  ObjectMapper objectMapper = new ObjectMapper();
  @Autowired RewardService rewardService;

  @Autowired TransMessageSender sender;

  /** 处理消息 声明交换机 ， 队列 ，绑定关系，定义消费方式 */
  @Override
  public void receiveMessage(Message message) {

    final String msg = new String(message.getBody());
    log.info("交易完成 。。开始积分:messageBody:{}", msg);

    try {
      final OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
      Reward reward = new Reward();
      reward.setOrderId(orderMessageDTO.getOrderId());
      reward.setStatus(RewardStatusEnum.SUCCESS);
      reward.setAmount(orderMessageDTO.getPrice());
      reward.setDate(LocalDateTime.now());
      rewardService.save(reward);
      orderMessageDTO.setRewardId(reward.getId());
      log.info("积分完成。。。。准备结束订单:settlementOrderDTO:{}", orderMessageDTO);
      sender.send(
          ExchangeEnum.ORDER_REWARD.getCode(), RoutingKey.KEY_ORDER.getCode(), orderMessageDTO);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
