package com.boo.ribbitmq.restaurant.config;

import com.boo.ribbitmq.restaurant.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.ribbitmq.restaurant.enums.RabbitComponentEnum.QueueEnum;
import com.boo.ribbitmq.restaurant.enums.RabbitComponentEnum.RoutingKey;
import com.boo.ribbitmq.restaurant.service.impl.RestaurantMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
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
   * 餐厅交换机声明
   *
   * @return {@link DirectExchange}
   */
  @Bean
  public DirectExchange restaurantExchange() {
    return new DirectExchange(ExchangeEnum.ORDER_RESTAURANT.getCode());
  }

  /**
   * 餐厅队列声明
   *
   * @return {@link Queue}
   */
  @Bean
  public Queue restaurantQueue() {
    return new Queue(QueueEnum.RESTAURANT.getCode());
  }

  /**
   * 餐厅-订单队列绑定
   *
   * @param restaurantExchange 餐厅交换
   * @param restaurantQueue 命令队列
   * @return {@link Binding}
   */
  @Bean
  public Binding restaurantBinding(
      @Autowired DirectExchange restaurantExchange, @Autowired Queue restaurantQueue) {
    return new Binding(
        restaurantQueue.getName(),
        DestinationType.QUEUE,
        restaurantExchange.getName(),
        RoutingKey.KEY_RESTAURANT.getCode(),
        null);
  }


  /** 监听队列名称 */
  private static final String QUEUE_NAME = QueueEnum.RESTAURANT.getCode();

  @Bean
  public SimpleMessageListenerContainer simpleMessageListenerContainer(
      @Autowired ConnectionFactory connectionFactory,
      @Autowired RestaurantMessageServiceImpl restaurantMessageService) {

    SimpleMessageListenerContainer container =
        new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(QUEUE_NAME);
    container.setExposeListenerChannel(true);
    container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    container.setMessageListener(restaurantMessageService);
    return container;
  }
}
