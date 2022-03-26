package com.boo.ribbitmq.restaurant.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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
  public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    //    发送消息时检查状态，提供了returnCallback时才适用
    rabbitTemplate.setMandatory(true);
    rabbitTemplate.setReturnsCallback(
        new RabbitTemplate.ReturnsCallback() {
          /**
           * Returned message callback.
           *
           * @param returned the returned message and metadata.
           */
          @Override
          public void returnedMessage(ReturnedMessage returned) {
            log.info("ReturnsCallback... 发送失败！ReturnedMessage :[{}]", returned);
          }
        });
    rabbitTemplate.setConfirmCallback(
        new RabbitTemplate.ConfirmCallback() {
          /**
           * Confirmation callback.
           *
           * @param correlationData correlation data for the callback.
           * @param ack true for ack, false for nack
           * @param cause An optional cause, for nack, when available, otherwise null.
           */
          @Override
          public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            log.info(
                "ConfirmCallback... correlationData: [{}],ack:[{}],cause:[{}]",
                correlationData,
                ack,
                cause);
          }
        });
    return rabbitTemplate;
  }
}
