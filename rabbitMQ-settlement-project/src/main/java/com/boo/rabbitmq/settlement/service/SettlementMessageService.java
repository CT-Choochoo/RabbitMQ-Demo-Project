package com.boo.rabbitmq.settlement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.Message;

/**
 * 结算信息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface SettlementMessageService {

  /**
   * 处理消息 @param message 消息
   *
   * @throws InterruptedException 中断异常
   */
  void handleMessage(Message message) throws InterruptedException, JsonProcessingException;
}
