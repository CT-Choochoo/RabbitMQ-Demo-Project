package com.boo.rabbitmq.deliveryman.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.boo.moodymq.listener.AbstractMessageListener;
import com.boo.moodymq.sender.TransMessageSender;
import com.boo.rabbitmq.deliveryman.dto.OrderMessageDTO;
import com.boo.rabbitmq.deliveryman.entity.Deliveryman;
import com.boo.rabbitmq.deliveryman.enums.DeliverymanStatusEnum;
import com.boo.rabbitmq.deliveryman.enums.RabbitComponentEnum.ExchangeEnum;
import com.boo.rabbitmq.deliveryman.enums.RabbitComponentEnum.RoutingKey;
import com.boo.rabbitmq.deliveryman.service.DeliverymanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 送货人消息服务impl
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Slf4j
@Service
public class DeliverymanMessageServiceImpl extends AbstractMessageListener {

  @Autowired DeliverymanService deliverymanService;

  @Autowired TransMessageSender sender;

  ObjectMapper objectMapper = new ObjectMapper();

  public static final String EXCHANGE_DELIVERYMAN = ExchangeEnum.ORDER_DELIVERYMAN.getCode();

  public static final String ROUTING_KEY_ORDER = RoutingKey.KEY_ORDER.getCode();

  @Override
  public void receiveMessage(Message message) {
    final String msg = new String(message.getBody());
    log.info("骑手端收到消息:messageBody:{}", msg);
    try {
      final OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
      final List<Deliveryman> list =
          deliverymanService.list(
              new LambdaQueryWrapper<Deliveryman>()
                  .eq(Deliveryman::getStatus, DeliverymanStatusEnum.AVALIABLE));

      final Deliveryman deliveryman = list.get(0);
      orderMessageDTO.setDeliverymanId(deliveryman.getId());
      log.info("骑手已选定 发回消息给order:restaurantOrderMessageDTO:{}", orderMessageDTO);

      sender.send(EXCHANGE_DELIVERYMAN, ROUTING_KEY_ORDER, orderMessageDTO);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
