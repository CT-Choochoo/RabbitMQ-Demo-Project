package com.boo.rabbitmq.reward.config;

import com.boo.rabbitmq.reward.service.RewardMessageService;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbit配置 spring启动时加载执行 orderMessageService.handleMessage()
 *
 * @author gaobo
 * @date 2022/03/18
 */
@Slf4j
@Configuration
public class RabbitConfig {
  @Autowired RewardMessageService messageService;

  @Autowired
  public void startListenMessage() throws IOException, TimeoutException, InterruptedException {
    messageService.handleMessage();
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    return new ConnectionFactory() {
      {
        setHost("localhost");
        setUsername("admin");
        setPassword("admin");
      }
    };
  }
}
