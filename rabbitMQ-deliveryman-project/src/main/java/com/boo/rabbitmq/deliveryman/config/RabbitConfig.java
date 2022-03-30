package com.boo.rabbitmq.deliveryman.config;

import com.boo.rabbitmq.deliveryman.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.rabbitmq.deliveryman.enums.RabbitComponentEnum.QueueEnum;
import com.boo.rabbitmq.deliveryman.enums.RabbitComponentEnum.RoutingKey;
import com.boo.rabbitmq.deliveryman.service.impl.DeliverymanMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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

  /**
   * 骑手交换机声明
   *
   * @return {@link FanoutExchange}
   */
  @Bean
  public DirectExchange deliverymanExchange() {
    return new DirectExchange(ExchangeEnum.ORDER_DELIVERYMAN.getCode());
  }

  /**
   * 骑手队列声明
   *
   * @return {@link Queue}
   */
  @Bean
  public Queue deliverymanQueue() {
    return new Queue(QueueEnum.DELIVERYMAN.getCode());
  }

  /**
   * 骑手-订单队列绑定
   *
   * @param deliverymanExchange 餐厅交换
   * @param deliverymanQueue 命令队列
   * @return {@link Binding}
   */
  @Bean
  public Binding deliverymanBinding(
      @Autowired DirectExchange deliverymanExchange, @Autowired Queue deliverymanQueue) {
    return new Binding(
        deliverymanQueue.getName(),
        DestinationType.QUEUE,
        deliverymanExchange.getName(),
        RoutingKey.KEY_DELIVERYMAN.getCode(),
        null);
  }

  /** 监听队列名称 */
  private static final String QUEUE_NAME = QueueEnum.DELIVERYMAN.getCode();

  @Bean
  public SimpleMessageListenerContainer simpleMessageListenerContainer(
      @Autowired ConnectionFactory connectionFactory,
      @Autowired DeliverymanMessageServiceImpl deliverymanMessageService) {

    SimpleMessageListenerContainer container =
        new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(QUEUE_NAME);
    container.setExposeListenerChannel(true);
    container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    container.setMessageListener(deliverymanMessageService);
    return container;
  }
}
