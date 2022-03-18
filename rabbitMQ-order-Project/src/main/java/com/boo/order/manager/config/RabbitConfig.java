package com.boo.order.manager.config;

import com.boo.order.manager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.springframework.context.annotation.Lazy;

/**
 * rabbit配置 spring启动时加载执行 orderMessageService.handleMessage()
 *
 * @author gaobo
 * @date 2022/03/18
 */
@Slf4j
@Configuration
public class RabbitConfig {

  final OrderMessageService orderMessageService;

  @Lazy
  public RabbitConfig(OrderMessageService orderMessageService) {
    this.orderMessageService = orderMessageService;
  }

  @Autowired
  public void startListenMessage() throws IOException, TimeoutException, InterruptedException {
    orderMessageService.handleMessage();
  }
}
