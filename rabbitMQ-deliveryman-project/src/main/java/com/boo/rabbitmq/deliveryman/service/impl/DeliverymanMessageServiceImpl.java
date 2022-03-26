package com.boo.rabbitmq.deliveryman.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.boo.rabbitmq.deliveryman.dto.OrderMessageDTO;
import com.boo.rabbitmq.deliveryman.entity.Deliveryman;
import com.boo.rabbitmq.deliveryman.enums.DeliverymanStatusEnum;
import com.boo.rabbitmq.deliveryman.service.DeliverymanMessageService;
import com.boo.rabbitmq.deliveryman.service.DeliverymanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 送货人消息服务impl
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Slf4j
@Service
public class DeliverymanMessageServiceImpl implements DeliverymanMessageService {

  @Autowired DeliverymanService deliverymanService;

  @Autowired RabbitTemplate rabbitTemplate;

  ObjectMapper objectMapper = new ObjectMapper();

  @Value("${rabbitComponent.exchange.deliveryman}")
  private String exchangeDeliveryman;

  @Value("${rabbitComponent.routingKey.order}")
  private String routingKeyOrder;

  /** 处理消息 @throws IOException ioexception */
  @RabbitListener(
      bindings = {
        @QueueBinding(
            value =
                @Queue(
                    name = "${rabbitComponent.queue.deliveryman}",
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
            exchange = @Exchange(name = "${rabbitComponent.exchange.deliveryman}"),
            key = "${rabbitComponent.routingKey.deliveryman}")
      })
  @Override
  public void handleMessage(@Payload Message message) throws IOException {
    final String msg = new String(message.getBody());
    log.info("骑手端收到消息:messageBody:{}", msg);
    final OrderMessageDTO orderMessageDTO = objectMapper.readValue(msg, OrderMessageDTO.class);
    final List<Deliveryman> list =
        deliverymanService.list(
            new LambdaQueryWrapper<Deliveryman>()
                .eq(Deliveryman::getStatus, DeliverymanStatusEnum.AVALIABLE));

    final Deliveryman deliveryman = list.get(0);
    orderMessageDTO.setDeliverymanId(deliveryman.getId());
    log.info("骑手已选定 发回消息给order:restaurantOrderMessageDTO:{}", orderMessageDTO);

    String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
    rabbitTemplate.send(
        exchangeDeliveryman,
        routingKeyOrder,
        new Message(messageToSend.getBytes()),
        new CorrelationData() {
          {
            setId(orderMessageDTO.getOrderId().toString());
          }
        });
  }
}
