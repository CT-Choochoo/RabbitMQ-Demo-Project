package com.boo.rabbitmq.reward.service.impl;

import com.boo.rabbitmq.reward.dto.OrderMessageDTO;
import com.boo.rabbitmq.reward.entity.Reward;
import com.boo.rabbitmq.reward.enums.RewardStatusEnum;
import com.boo.rabbitmq.reward.service.RewardMessageService;
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
public class RewardMessageServiceImpl implements RewardMessageService {

  ObjectMapper objectMapper = new ObjectMapper();
  @Autowired RewardService rewardService;

  @Autowired RabbitTemplate rabbitTemplate;

  @Value("${rabbitComponent.exchange.reward}")
  private String exchangeReward;

  @Value("${rabbitComponent.routingKey.order}")
  private String routingKeyOrder;

  /** 处理消息 声明交换机 ， 队列 ，绑定关系，定义消费方式 */
  @RabbitListener(
      bindings = {
        @QueueBinding(
            value =
                @Queue(
                    name = "${rabbitComponent.queue.reward}",
                    arguments = {
                      @Argument(name = "x-message-ttl", value = "15000", type = "java.lang.Long"),
                      @Argument(
                          name = "x-dead-letter-exchange",
                          value = "${rabbitComponent.exchange.dlx}"),
                      @Argument(name = "x-max-length", value = "20", type = "java.lang.Long")
                    }),
            exchange =
                @Exchange(name = "${rabbitComponent.exchange.reward}", type = ExchangeTypes.TOPIC),
            key = "${rabbitComponent.routingKey.reward}")
      })
  @Override
  public void handleMessage(@Payload Message message) throws JsonProcessingException {

    final String msg = new String(message.getBody());
    log.info("交易完成 。。开始积分:messageBody:{}", msg);

    final OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
    Reward reward = new Reward();
    reward.setOrderId(orderMessageDTO.getOrderId());
    reward.setStatus(RewardStatusEnum.SUCCESS);
    reward.setAmount(orderMessageDTO.getPrice());
    reward.setDate(LocalDateTime.now());
    rewardService.save(reward);
    orderMessageDTO.setRewardId(reward.getId());
    log.info("积分完成。。。。准备结束订单:settlementOrderDTO:{}", orderMessageDTO);
    final byte[] bytes = objectMapper.writeValueAsBytes(orderMessageDTO);
    rabbitTemplate.send(
        exchangeReward,
        routingKeyOrder,
        new Message(bytes),
        new CorrelationData() {
          {
            setId(orderMessageDTO.getOrderId().toString());
          }
        });
  }
}
