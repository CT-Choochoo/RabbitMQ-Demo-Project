package com.boo.rabbitmq.reward.config;

import com.boo.rabbitmq.reward.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.rabbitmq.reward.enums.RabbitComponentEnum.QueueEnum;
import com.boo.rabbitmq.reward.enums.RabbitComponentEnum.RoutingKey;
import com.boo.rabbitmq.reward.service.impl.RewardMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbit配置
 *
 * @author gaobo
 * @date 2022/03/18
 */
@Slf4j
@Configuration
public class RabbitConfig {

  /**
   * 积分路由
   *
   * @return {@link TopicExchange}
   */
  @Bean
  public TopicExchange rewardExchange() {
    return new TopicExchange(ExchangeEnum.ORDER_REWARD.getCode());
  }

  /**
   * 积分队列
   *
   * @return {@link Queue}
   */
  @Bean
  public Queue rewardQueue() {
    return new Queue(QueueEnum.REWARD.getCode());
  }

  /**
   * 积分路由-订单队列绑定
   *
   * @param rewardExchange 餐厅交换
   * @param rewardQueue 命令队列
   * @return {@link Binding}
   */
  @Bean
  public Binding rewardBinding(
      @Autowired TopicExchange rewardExchange, @Autowired Queue rewardQueue) {
    return new Binding(
        rewardQueue.getName(),
        DestinationType.QUEUE,
        rewardExchange.getName(),
        RoutingKey.KEY_REWARD.getCode(),
        null);
  }

  /** 监听队列名称 */
  private static final String QUEUE_NAME = QueueEnum.REWARD.getCode();

  @Bean
  public SimpleMessageListenerContainer simpleMessageListenerContainer(
      @Autowired ConnectionFactory connectionFactory,
      @Autowired RewardMessageServiceImpl rewardMessageService) {

    SimpleMessageListenerContainer container =
        new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(QUEUE_NAME);
    container.setExposeListenerChannel(true);
    container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    container.setMessageListener(rewardMessageService);
    return container;
  }
}
