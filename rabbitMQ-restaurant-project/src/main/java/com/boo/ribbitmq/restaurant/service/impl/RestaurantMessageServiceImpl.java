package com.boo.ribbitmq.restaurant.service.impl;

import com.boo.ribbitmq.restaurant.dto.OrderMessageDTO;
import com.boo.ribbitmq.restaurant.entity.Product;
import com.boo.ribbitmq.restaurant.entity.Restaurant;
import com.boo.ribbitmq.restaurant.enums.ProductStatusEnum;
import com.boo.ribbitmq.restaurant.enums.RestaurantStatusEnum;
import com.boo.ribbitmq.restaurant.service.ProductService;
import com.boo.ribbitmq.restaurant.service.RestaurantMessageService;
import com.boo.ribbitmq.restaurant.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 餐馆消息服务实现
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Service
@Slf4j
public class RestaurantMessageServiceImpl implements RestaurantMessageService {

  @Autowired ProductService productService;
  @Autowired RestaurantService restaurantService;
  @Autowired RabbitTemplate rabbitTemplate;
  ObjectMapper objectMapper = new ObjectMapper();

  @Value(value = "${rabbitComponent.exchange.restaurant}")
  public String exchangeRestaurant;

  @Value(value = "${rabbitComponent.routingKey.order}")
  public String routingKeyOrder;
  /**
   * 处理消息
   *
   * @param message 消息
   * @throws IOException ioexception
   */
  @RabbitListener(
      bindings = {
        @QueueBinding(
            value =
                @Queue(
                    value = "${rabbitComponent.queue.restaurant}",
                    arguments = {
                      @Argument(
                          name = "x-message-ttl",
                          value = "15000",
                          type = "java.lang.Integer"),
                      @Argument(name = "x-max-length", value = "20", type = "java.lang.Integer"),
                      @Argument(
                          name = "x-dead-letter-exchange",
                          value = "${rabbitComponent.exchange.dlx}")
                    }),
            exchange = @Exchange("${rabbitComponent.exchange.restaurant}"),
            key = "${rabbitComponent.routingKey.restaurant}")
      })
  @Override
  public void handleMessage(@Payload Message message) throws IOException {
    //        1. 创建连接工厂定义连接信息
    //        2. 从queue.restaurant中获取对象判断消息状态，如果是创建订单，则改为商家确认并设置确认字段，商家信息，商品信息，商品价格
    //        3. 将新的实体放入queue.order消息队列中

    log.info("骑手接收到消息：[{}]", new String(message.getBody()));
    final OrderMessageDTO orderMessageDTO =
        objectMapper.readValue(message.getBody(), OrderMessageDTO.class);

    final Integer productId = orderMessageDTO.getProductId();
    final Product product = productService.getById(productId);
    final Integer restaurantId = product.getRestaurantId();
    final Restaurant restaurant = restaurantService.getById(restaurantId);
    if (ProductStatusEnum.AVALIABLE == product.getStatus()
        && RestaurantStatusEnum.OPEN == restaurant.getStatus()) {
      orderMessageDTO.setConfirmed(true);
      orderMessageDTO.setPrice(product.getPrice());
    } else {
      orderMessageDTO.setConfirmed(false);
    }

    String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);

    //        模拟处理过程
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    final CorrelationData correlationData = new CorrelationData();
    correlationData.setId(orderMessageDTO.getOrderId().toString());
    log.info("发送给order，{}", messageToSend);
    rabbitTemplate.convertAndSend(
        exchangeRestaurant, routingKeyOrder, messageToSend, correlationData);
  }
}
