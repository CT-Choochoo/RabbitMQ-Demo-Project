package com.boo.ribbitmq.restaurant.service.impl;

import com.boo.moodymq.listener.AbstractMessageListener;
import com.boo.moodymq.sender.TransMessageSender;
import com.boo.ribbitmq.restaurant.dto.OrderMessageDTO;
import com.boo.ribbitmq.restaurant.entity.Product;
import com.boo.ribbitmq.restaurant.entity.Restaurant;
import com.boo.ribbitmq.restaurant.enums.ProductStatusEnum;
import com.boo.ribbitmq.restaurant.enums.RabbitComponentEnum;
import com.boo.ribbitmq.restaurant.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.ribbitmq.restaurant.enums.RabbitComponentEnum.RoutingKey;
import com.boo.ribbitmq.restaurant.enums.RestaurantStatusEnum;
import com.boo.ribbitmq.restaurant.service.ProductService;
import com.boo.ribbitmq.restaurant.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 餐馆消息服务实现
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Service
@Slf4j
public class RestaurantMessageServiceImpl extends AbstractMessageListener {

  @Autowired ProductService productService;
  @Autowired RestaurantService restaurantService;
  ObjectMapper objectMapper = new ObjectMapper();
  @Autowired TransMessageSender sender;

  public static final String EXCHANGE_RESTAURANT = ExchangeEnum.ORDER_RESTAURANT.getCode();

  public static final String ROUTING_KEY_ORDER = RoutingKey.KEY_ORDER.getCode();

  /**
   * 处理消息
   *
   * @param message 消息
   * @throws IOException IOexception
   */
  @Override
  public void receiveMessage(Message message) {
    log.info("骑手接收到消息：[{}]", new String(message.getBody()));
    final OrderMessageDTO orderMessageDTO;
    try {
      orderMessageDTO = objectMapper.readValue(message.getBody(), OrderMessageDTO.class);
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

      sender.send(EXCHANGE_RESTAURANT, ROUTING_KEY_ORDER, orderMessageDTO);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
