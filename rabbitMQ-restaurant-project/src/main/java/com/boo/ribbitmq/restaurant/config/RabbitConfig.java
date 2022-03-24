package com.boo.ribbitmq.restaurant.config;

import com.boo.ribbitmq.restaurant.service.RestaurantMessageService;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
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
  @Autowired RestaurantMessageService messageService;

  //  @Autowired
  //  public void startListenMessage() throws IOException, TimeoutException, InterruptedException {
  //    messageService.handleMessage();
  //  }

  @Bean
  public Exchange restaurantExchange() {
    return new DirectExchange("exchange.order.deliveryman");
  }

  @Bean
  public Queue deliverymanQueue() {
    final HashMap<String, Object> map = new HashMap<>(16);
    //      设置超时时间 超过时间进入死信队列
    map.put("x-message-ttl", 150000);
    //      设置队列最大长度  超过长度进入死信队列
    map.put("x-max-length", 5);
    //      设置死信交换机
    map.put("x-dead-letter-exchange", "exchange.dlx");
    return new Queue("queue.restaurant", true, false, false, map);
  }

  @Bean
  public void Binding() {
    new Binding(
        "queue.restaurant",
        DestinationType.QUEUE,
        "exchange.order.deliveryman",
        "key.restauran",
        null);
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
            log.info(
                "ConfirmCallback... correlationData: [{}],ack:[{}],cause:[{}]",
                correlationData,
                ack,
                cause);
          }
        });
    return rabbitTemplate;
  }

  @Bean
  public SimpleMessageListenerContainer messageListenerContainer(
      ConnectionFactory connectionFactory) {
    SimpleMessageListenerContainer messageListenerContainer =
        new SimpleMessageListenerContainer(connectionFactory);
    //    设置监听队列
    messageListenerContainer.setQueueNames("queue.restaurant");
    messageListenerContainer.setConcurrentConsumers(3);
    messageListenerContainer.setMaxConcurrentConsumers(5);
    messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
    messageListenerContainer.setMessageListener(message -> log.info("message:{}", message));
    messageListenerContainer.setPrefetchCount(2);
    messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    messageListenerContainer.setMessageListener(
        (ChannelAwareMessageListener)
            (message, channel) -> {
              messageService.publishHandle(message.getBody());
              assert channel != null;
              //     设置手动签收多条
              //        if (msg.getEnvelope().getDeliveryTag() % 3 == 0) {
              //          channel.basicAck(msg.getEnvelope().getDeliveryTag(), true);
              //        }
              //     设置手动签收单条
              channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
              //     设置手动拒收,  (标记，是否批量，是否重回队列-如果不回队列，切配置了死信则会回到死信队列中)
              //        channel.basicNack(msg.getEnvelope().getDeliveryTag(), false, false);
            });
    messageListenerContainer.start();
    return messageListenerContainer;
  }
}
