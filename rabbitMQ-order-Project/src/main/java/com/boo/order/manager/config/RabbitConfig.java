package com.boo.order.manager.config;

import com.boo.order.manager.service.OrderMessageService;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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
  @Autowired OrderMessageService messageService;

  @Autowired
  public void startListenMessage() throws IOException, TimeoutException, InterruptedException {
    messageService.handleMessage();
  }
  /*---------------------restaurant---------------------*/
  @Bean
  public Exchange exchange1() {
    return new DirectExchange("exchange.order.restaurant");
  }

  @Bean
  public Queue queue1() {
    return new Queue("queue.order");
  }

  @Bean
  public Binding binding1() {
    return new Binding(
        "queue.order",
        Binding.DestinationType.QUEUE,
        "exchange.order.deliveryman",
        "key.order",
        null);
  }

  /*---------------------deliveryman---------------------*/
  @Bean
  public Exchange exchange2() {
    return new DirectExchange("exchange.order.deliveryman");
  }

  @Bean
  public Binding binding2() {
    return new Binding(
        "queue.order",
        Binding.DestinationType.QUEUE,
        "exchange.order.deliveryman",
        "key.order",
        null);
  }

  /*---------settlement---------*/
  @Bean
  public Exchange exchange3() {
    return new FanoutExchange("exchange.order.settlement");
  }

  @Bean
  public Exchange exchange4() {
    return new FanoutExchange("exchange.settlement.order");
  }

  @Bean
  public Binding binding3() {
    return new Binding(
        "queue.order",
        Binding.DestinationType.QUEUE,
        "exchange.settlement.order",
        "key.order",
        null);
  }

  /*--------------reward----------------*/
  @Bean
  public Exchange exchange5() {
    return new TopicExchange("exchange.order.reward");
  }

  @Bean
  public Binding binding4() {
    return new Binding(
        "queue.order", Binding.DestinationType.QUEUE, "exchange.order.reward", "key.order", null);
  }

  /*---------------------restaurant---------------------*/
  @Bean
  public Exchange exchange6() {
    return new TopicExchange("exchange.dlx");
  }

  @Bean
  public Queue queue2() {
    return new Queue("queue.dlx");
  }

  @Bean
  public Binding binding5() {
    return new Binding(
        "queue.order", Binding.DestinationType.QUEUE, "exchange.order.deliveryman", "#", null);
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    connectionFactory.setHost("127.0.0.1");
    connectionFactory.setPort(5672);
    connectionFactory.setPassword("admin");
    connectionFactory.setUsername("admin");
    //    连接connection ，使connection之前的声明生效
    connectionFactory.createConnection();
    connectionFactory.setPublisherConfirmType(ConfirmType.CORRELATED);
    connectionFactory.setPublisherReturns(true);
    return connectionFactory;
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
    rabbitAdmin.setAutoStartup(true);
    return rabbitAdmin;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
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
            log.info("ConfirmCallback... correlationData: [{}],ack:[{}],cause:[{}]", correlationData, ack, cause);
          }
        });
    return rabbitTemplate;
  }
}
