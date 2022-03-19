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
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 餐馆消息服务实现
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Service
public class RestaurantMessageServiceImpl implements RestaurantMessageService {

  @Autowired ProductService productService;
  @Autowired RestaurantService restaurantService;
  ObjectMapper objectMapper = new ObjectMapper();
  /** 处理消息 */
  @Override
  public void handleMessage() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("admin");
    factory.setPassword("admin");

    try (Connection connection = factory.newConnection()) {
      //      1.创建信道
      final Channel channel = connection.createChannel();
      //      2.声明交换机（name,type,是否持久化，如果未被使用是否自动删除，其他参数）
      channel.exchangeDeclare(
          "exchange.order.restaurant", BuiltinExchangeType.DIRECT, true, false, null);
      //      3，声明队列 （名称，是否持久化，是否独占，如果未被使用是都删除，其他参数）
      channel.queueDeclare("queue.restaurant", true, false, false, null);
      //      4.绑定队列和交换机设置路由key
      channel.queueBind("queue.restaurant", "exchange.order.restaurant", "key.restaurant");
      //      5.生成consumerTag
      channel.basicConsume("queue.restaurant", true, deliverCallback, consumerTag -> {});
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
    }
  }

  /** 提供回调 */
  DeliverCallback deliverCallback =
      (tag, msg) -> {
        //        1. 创建连接工厂定义连接信息
        //        2. 从queue.restaurant中获取对象判断消息状态，如果是创建订单，则改为商家确认并设置确认字段，商家信息，商品信息，商品价格
        //        3. 将新的实体放入queue.order消息队列中
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("admin");
        try (Connection connection = factory.newConnection()) {

          final byte[] body = msg.getBody();
          final OrderMessageDTO orderMessageDTO =
              objectMapper.readValue(body, OrderMessageDTO.class);

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

          final Channel channel = connection.createChannel();
          String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
          channel.basicPublish(
              "exchange.order.restaurant",
              "key.order",
              null,
              messageToSend.getBytes(StandardCharsets.UTF_8));
        } catch (TimeoutException e) {
          e.printStackTrace();
        }
      };
}
