package com.boo.rabbitmq.deliveryman.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.core.Message;

/**
 * 送货人消息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface DeliverymanMessageService {

  /**
   * 处理消息 @throws IOException ioexception
   *
   * @throws TimeoutException 超时异常
   * @throws InterruptedException 中断异常
   */
  void handleMessage(Message message) throws IOException, TimeoutException, InterruptedException;
}
