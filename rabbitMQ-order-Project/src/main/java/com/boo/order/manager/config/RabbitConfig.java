package com.boo.order.manager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

  @Bean
  public RabbitTemplate createRabbitTemplate(@Autowired ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate();
    rabbitTemplate.setConnectionFactory(connectionFactory);
    // 设置开启Mandatory，才能触发回调函数，无论消息推送结果怎么样都强制调用回调函数
    rabbitTemplate.setMandatory(true);
    rabbitTemplate.setConfirmCallback(
        (correlationData, b, s) -> {
          log.info("ConfirmCallback    ：相关数据：" + correlationData);
          log.info("ConfirmCallback    ：确认情况：" + b);
          log.info("ConfirmCallback    ：原因：" + s);
        });

    rabbitTemplate.setReturnsCallback(
        returned -> {
          log.info("ReturnCallback    ：消息：" + returned.getMessage());
          log.info("ReturnCallback    ：回应码：" + returned.getReplyCode());
          log.info("ReturnCallback    ：回应消息：" + returned.getReplyText());
          log.info("ReturnCallback    ：交换机：" + returned.getExchange());
          log.info("ReturnCallback    ：路由键：" + returned.getRoutingKey());
        });
    return rabbitTemplate;
  }
}
