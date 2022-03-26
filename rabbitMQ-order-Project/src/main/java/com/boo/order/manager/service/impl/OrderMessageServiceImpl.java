package com.boo.order.manager.service.impl;

import com.boo.order.manager.dto.OrderMessageDTO;
import com.boo.order.manager.enums.OrderStatusEnum;
import com.boo.order.manager.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.order.manager.enums.RabbitComponentEnum.RoutingKey;
import com.boo.order.manager.po.OrderDetail;
import com.boo.order.manager.service.OrderDetailService;
import com.boo.order.manager.service.OrderMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 订单消息服务impl
 *
 * @author gaobo
 * @date 2022/03/24
 */
@Slf4j
@Service
public class OrderMessageServiceImpl implements OrderMessageService {

  ObjectMapper objectMapper = new ObjectMapper();
  @Autowired OrderDetailService orderDetailService;
  @Autowired RabbitTemplate rabbitTemplate;

  @RabbitListener(
      bindings = {
        // 绑定订单队列和商家交换机
        @QueueBinding(
            value =
                @Queue(
                    name = "${rabbitComponent.queue.oder}",
                    arguments = {
                      @Argument(name = "x-message-ttl", value = "1000", type = "java.lang.Long"),
                      @Argument(
                          name = "x-dead-letter-exchange",
                          value = "${rabbitComponent.exchange.dlx}"),
                      @Argument(name = "x-max-length", value = "20", type = "java.lang.Long")
                    }),
            exchange = @Exchange(name = "${rabbitComponent.exchange.restaurant}"),
            key = "${rabbitComponent.routingKey.order}"),
        //  绑定订单队列和骑手交换机
        @QueueBinding(
            value =
                @Queue(
                    name = "${rabbitComponent.queue.oder}",
                    arguments = {
                      @Argument(name = "x-message-ttl", value = "1000", type = "java.lang.Long"),
                      @Argument(
                          name = "x-dead-letter-exchange",
                          value = "${rabbitComponent.exchange.dlx}"),
                      @Argument(name = "x-max-length", value = "20", type = "java.lang.Long")
                    }),
            exchange = @Exchange(name = "${rabbitComponent.exchange.deliveryman}"),
            key = "${rabbitComponent.routingKey.order}"),
        //          绑定订单队列和结算交换机
        @QueueBinding(
            value =
                @Queue(
                    name = "${rabbitComponent.queue.oder}",
                    arguments = {
                      @Argument(name = "x-message-ttl", value = "1000", type = "java.lang.Long"),
                      @Argument(
                          name = "x-dead-letter-exchange",
                          value = "${rabbitComponent.exchange.dlx}"),
                      @Argument(name = "x-max-length", value = "20", type = "java.lang.Long")
                    }),
            exchange =
                @Exchange(
                    name = "${rabbitComponent.exchange.settlementOrder}",
                    type = ExchangeTypes.FANOUT),
            key = "${rabbitComponent.routingKey.order}"),
        //          绑定订单队列和积分交换机
        @QueueBinding(
            value =
                @Queue(
                    name = "${rabbitComponent.queue.oder}",
                    arguments = {
                      @Argument(name = "x-message-ttl", value = "1000", type = "java.lang.Long"),
                      @Argument(
                          name = "x-dead-letter-exchange",
                          value = "${rabbitComponent.exchange.dlx}"),
                      @Argument(name = "x-max-length", value = "20", type = "java.lang.Long")
                    }),
            exchange =
                @Exchange(name = "${rabbitComponent.exchange.reward}", type = ExchangeTypes.TOPIC),
            key = "${rabbitComponent.routingKey.order}"),
        //          绑定死信队列
        @QueueBinding(
            value = @Queue(name = "${rabbitComponent.queue.dlx}"),
            exchange = @Exchange(name = "${rabbitComponent.exchange.dlx}"),
            key = "${rabbitComponent.routingKey.all}"),
      })
  @Override
  public void handleMessage(@Payload Message message) {
    try {
      final String s = new String(message.getBody());
      final OrderMessageDTO body = objectMapper.readValue(s, OrderMessageDTO.class);
      //          获取队列中的对象,判断阶段做对应处处理
      OrderDetail entity = orderDetailService.getById(body.getOrderId());
      switch (entity.getStatus()) {
        case ORDER_CREATING:
          log.info("订单创建完成，商家确认完毕，开始发送给骑手。。。。。");
          executeOrderCreating(body, entity);
          break;
        case RESTAURANT_CONFIRMED:
          log.info("骑手已确认，更新信息，开始结算。。。。。");
          executeRestaurantConfirmed(body, entity);
          break;
        case DELIVERYMAN_CONFIRMED:
          log.info("结算信息已确认，开始计算积分。。。。。");
          executeDeliverymanConfirmed(body, entity);
          break;
        case SETTLEMENT_CONFIRMED:
          log.info("订单完成！");
          if (null != body.getRewardId()) {
            entity.setStatus(OrderStatusEnum.ORDER_CREATED);
            entity.setRewardId(body.getRewardId());
          } else {
            entity.setStatus(OrderStatusEnum.FAILED);
          }
          orderDetailService.updateById(entity);
          break;
        default:
          break;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  ;

  /**
   * 送货人执行确认
   *
   * @param orderMessageDTO 订单消息dto
   * @param entity 实体
   */
  private void executeDeliverymanConfirmed(OrderMessageDTO orderMessageDTO, OrderDetail entity)
      throws IOException {
    if (null != orderMessageDTO.getSettlementId()) {
      entity.setStatus(OrderStatusEnum.SETTLEMENT_CONFIRMED);
      entity.setSettlementId(orderMessageDTO.getSettlementId());
      orderDetailService.updateById(entity);
      String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
      rabbitTemplate.convertAndSend(
          ExchangeEnum.ORDER_REWARD.getCode(),
          RoutingKey.KEY_REWARD.getCode(),
          messageToSend.getBytes());
    } else {
      entity.setStatus(OrderStatusEnum.FAILED);
      orderDetailService.updateById(entity);
    }
  }

  /**
   * 执行餐厅确认步骤
   *
   * @param orderMessageDTO 订单消息dto
   * @param entity 实体
   * @throws IOException ioexception
   */
  private void executeRestaurantConfirmed(OrderMessageDTO orderMessageDTO, OrderDetail entity)
      throws IOException {

    if (null != orderMessageDTO.getDeliverymanId()) {
      entity.setStatus(OrderStatusEnum.DELIVERYMAN_CONFIRMED);
      entity.setDeliverymanId(orderMessageDTO.getDeliverymanId());
      orderDetailService.updateById(entity);
      String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
      rabbitTemplate.convertAndSend(
          ExchangeEnum.ORDER_SETTLEMENT.getCode(),
          RoutingKey.KEY_SETTLEMENT.getCode(),
          messageToSend.getBytes());

    } else {
      entity.setStatus(OrderStatusEnum.FAILED);
      orderDetailService.updateById(entity);
    }
  }

  /**
   * 执行订单创建
   *
   * <p>1. 更新数据库信息，a.订单信息状态变更 b.商品价格定义
   *
   * <p>2. 将DTO放入消息队列
   *
   * @param orderMessageDTO 订单消息dto
   * @param entity 实体
   * @throws IOException IOexception
   */
  private void executeOrderCreating(OrderMessageDTO orderMessageDTO, OrderDetail entity)
      throws IOException {

    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
      entity.setStatus(OrderStatusEnum.RESTAURANT_CONFIRMED);
      entity.setPrice(orderMessageDTO.getPrice());
      orderDetailService.updateById(entity);
      String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
      rabbitTemplate.convertAndSend(
          ExchangeEnum.ORDER_DELIVERYMAN.getCode(),
          RoutingKey.KEY_DELIVERYMAN.getCode(),
          messageToSend.getBytes());

    } else {
      entity.setStatus(OrderStatusEnum.FAILED);
      orderDetailService.updateById(entity);
    }
  }
}
