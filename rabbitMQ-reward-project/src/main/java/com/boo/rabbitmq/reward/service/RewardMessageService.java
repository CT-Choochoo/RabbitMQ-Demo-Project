package com.boo.rabbitmq.reward.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.Message;

/**
 * 奖励消息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface RewardMessageService {

  void handleMessage(Message message) throws InterruptedException, JsonProcessingException;
}
