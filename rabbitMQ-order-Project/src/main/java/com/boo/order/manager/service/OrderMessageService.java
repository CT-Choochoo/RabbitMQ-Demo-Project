package com.boo.order.manager.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 订单信息服务
 *
 * @author gaobo
 * @date 2022/03/18
 */
public interface OrderMessageService {
  /**
   * 处理消息
   *
   * @throws IOException ioexception
   * @throws TimeoutException 超时异常
   * @throws InterruptedException 中断异常
   */
  void handleMessage() throws IOException, TimeoutException, InterruptedException;
}
