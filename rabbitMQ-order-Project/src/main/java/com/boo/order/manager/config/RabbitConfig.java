package com.boo.order.manager.config;

import com.boo.order.manager.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.order.manager.enums.RabbitComponentEnum.QueueEnum;
import com.boo.order.manager.enums.RabbitComponentEnum.RoutingKey;
import com.boo.order.manager.service.impl.OrderMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
   * 订单队列声明
   *
   * @return {@link Queue}
   */
  @Bean
  public Queue orderQueue() {
    return new Queue(QueueEnum.ORDER.getCode());
  }

  /**
   * 餐厅-订单队列绑定
   *
   * @param restaurantExchange 餐厅交换
   * @param orderQueue 命令队列
   * @return {@link Binding}
   */
  @Bean
  public Binding restaurantBinding(
      @Autowired DirectExchange restaurantExchange, @Autowired Queue orderQueue) {
    return new Binding(
        orderQueue.getName(),
        DestinationType.QUEUE,
        restaurantExchange.getName(),
        RoutingKey.KEY_ORDER.getCode(),
        null);
  }

  /**
   * 骑手交换机声明
   *
   * @return {@link DirectExchange}
   */
  @Bean
  public DirectExchange deliveryManExchange() {
    return new DirectExchange(ExchangeEnum.ORDER_DELIVERYMAN.getCode());
  }

  /**
   * 送货员绑定
   *
   * @param deliveryManExchange 餐厅交换
   * @param orderQueue 命令队列
   * @return {@link Binding}
   */
  @Bean
  public Binding deliveryManBinding(
      @Autowired DirectExchange deliveryManExchange, @Autowired Queue orderQueue) {
    return new Binding(
        orderQueue.getName(),
        DestinationType.QUEUE,
        deliveryManExchange.getName(),
        RoutingKey.KEY_ORDER.getCode(),
        null);
  }

  /**
   * 结算交易
   *
   * @return {@link FanoutExchange}
   */
  @Bean
  public FanoutExchange settlementExchange() {
    return new FanoutExchange(ExchangeEnum.SETTLEMENT_ORDER.getCode());
  }

  /**
   * 结算绑定
   *
   * @param settlementExchange 结算交易
   * @param orderQueue 命令队列
   * @return {@link Binding}
   */
  @Bean
  public Binding settlementBinding(
      @Autowired FanoutExchange settlementExchange, @Autowired Queue orderQueue) {
    return new Binding(
        orderQueue.getName(),
        DestinationType.QUEUE,
        settlementExchange.getName(),
        RoutingKey.KEY_ORDER.getCode(),
        null);
  }

  /**
   * 积分交换机
   *
   * @return {@link TopicExchange}
   */
  @Bean
  public TopicExchange rewardExchange() {
    return new TopicExchange(ExchangeEnum.ORDER_REWARD.getCode());
  }

  @Bean
  public Binding rewardBinding(
      @Autowired TopicExchange rewardExchange, @Autowired Queue orderQueue) {
    return new Binding(
        orderQueue.getName(),
        DestinationType.QUEUE,
        rewardExchange.getName(),
        RoutingKey.KEY_ORDER.getCode(),
        null);
  }

  /** 监听队列名称 */
  private static final String QUEUE_NAME = QueueEnum.ORDER.getCode();

  @Bean
  public SimpleMessageListenerContainer simpleMessageListenerContainer(
      @Autowired ConnectionFactory connectionFactory,
      @Autowired OrderMessageServiceImpl orderMessageService) {

    SimpleMessageListenerContainer container =
        new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(QUEUE_NAME);
    container.setExposeListenerChannel(true);
    container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    container.setMessageListener(orderMessageService);
    return container;
  }
}
