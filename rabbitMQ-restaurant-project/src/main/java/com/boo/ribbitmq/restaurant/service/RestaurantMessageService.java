package com.boo.ribbitmq.restaurant.service;

import java.io.IOException;
import org.springframework.amqp.core.Message;

/**
 * 餐馆消息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface RestaurantMessageService {

  /**
   * 处理消息
   *
   * @param message 消息
   * @throws IOException ioexception
   */
  void handleMessage(Message message) throws IOException;
}
