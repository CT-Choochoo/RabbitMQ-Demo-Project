package com.boo.order.manager.service;

import org.springframework.amqp.core.Message;

/**
 * 订单信息服务
 *
 * @author gaobo
 * @date 2022/03/18
 */
public interface OrderMessageService {

  /**
   * 发布处理
   *
   * @param message 消息byte[]
   */
  void handleMessage(Message message);
}
