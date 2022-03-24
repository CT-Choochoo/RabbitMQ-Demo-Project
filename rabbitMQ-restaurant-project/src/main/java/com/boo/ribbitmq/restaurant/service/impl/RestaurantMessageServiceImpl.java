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
import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

  @Lazy @Autowired ProductService productService;
  @Lazy @Autowired RestaurantService restaurantService;
  @Lazy @Autowired ConnectionFactory connectionFactory;
  ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void publishHandle(byte[] body) throws IOException {
    //        1. 创建连接工厂定义连接信息
    //        2. 从queue.restaurant中获取对象判断消息状态，如果是创建订单，则改为商家确认并设置确认字段，商家信息，商品信息，商品价格
    //        3. 将新的实体放入queue.order消息队列中
    log.info("骑手接收到消息：[{}]", new String(body));
    final OrderMessageDTO orderMessageDTO = objectMapper.readValue(body, OrderMessageDTO.class);

    final Connection connection = connectionFactory.createConnection();
    final Channel channel = connection.createChannel(false);

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
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    log.info("发送给order，{}", messageToSend);
    channel.basicPublish("exchange.order.restaurant", "key.order", null, messageToSend.getBytes());
  }
  ;
}
