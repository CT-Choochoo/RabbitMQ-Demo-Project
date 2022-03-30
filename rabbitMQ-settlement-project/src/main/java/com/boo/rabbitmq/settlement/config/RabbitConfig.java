package com.boo.rabbitmq.settlement.config;

import com.boo.rabbitmq.settlement.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.rabbitmq.settlement.enums.RabbitComponentEnum.QueueEnum;
import com.boo.rabbitmq.settlement.enums.RabbitComponentEnum.RoutingKey;
import com.boo.rabbitmq.settlement.service.impl.SettlementMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
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
   * 餐厅交换机声明
   *
   * @return {@link FanoutExchange}
   */
  @Bean
  public FanoutExchange settlementExchange() {
    return new FanoutExchange(ExchangeEnum.ORDER_SETTLEMENT.getCode());
  }

  /**
   * 餐厅队列声明
   *
   * @return {@link Queue}
   */
  @Bean
  public Queue settlementQueue() {
    return new Queue(QueueEnum.SETTLEMENT.getCode());
  }

  /**
   * 餐厅-订单队列绑定
   *
   * @param settlementExchange 餐厅交换
   * @param settlementQueue 命令队列
   * @return {@link Binding}
   */
  @Bean
  public Binding settlementBinding(
      @Autowired FanoutExchange settlementExchange, @Autowired Queue settlementQueue) {
    return new Binding(
        settlementQueue.getName(),
        DestinationType.QUEUE,
        settlementExchange.getName(),
        RoutingKey.KEY_SETTLEMENT.getCode(),
        null);
  }

  /** 监听队列名称 */
  private static final String QUEUE_NAME = QueueEnum.SETTLEMENT.getCode();

  @Bean
  public SimpleMessageListenerContainer simpleMessageListenerContainer(
      @Autowired ConnectionFactory connectionFactory,
      @Autowired SettlementMessageServiceImpl settlementMessageService) {

    SimpleMessageListenerContainer container =
        new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(QUEUE_NAME);
    container.setExposeListenerChannel(true);
    container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    container.setMessageListener(settlementMessageService);
    return container;
  }
}
