package com.boo.rabbitmq.deliveryman.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 送货人消息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface DeliverymanMessageService {

  /** 处理消息 */
  void handleMessage() throws IOException, TimeoutException, InterruptedException;
}
