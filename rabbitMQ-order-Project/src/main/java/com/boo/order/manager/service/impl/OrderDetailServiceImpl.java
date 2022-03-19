package com.boo.order.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.order.manager.convert.OrderDetailConvert;
import com.boo.order.manager.dao.OrderDetailMapper;
import com.boo.order.manager.dto.OrderMessageDTO;
import com.boo.order.manager.enums.OrderStatusEnum;
import com.boo.order.manager.po.OrderDetail;
import com.boo.order.manager.service.OrderDetailService;
import com.boo.order.manager.vo.OrderCreateVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单逻辑处理
 *
 * @author gaobo
 * @date 2022/03/18
 */
@Slf4j
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>
    implements OrderDetailService {

  ObjectMapper objectMapper = new ObjectMapper();

  @Autowired OrderDetailConvert orderDetailConvert;

  /**
   * 创建订单
   *
   * @param orderCreateVO 订单创建签证官
   * @throws IOException IO Exception
   * @throws TimeoutException 超时异常
   */
  @Override
  public void createOrder(OrderCreateVO orderCreateVO) throws IOException, TimeoutException {
    log.info("createOrder:orderCreateVO:{}", orderCreateVO);
    //  1.收到订单，更新状态和时间并保存
    OrderDetail orderDetail = orderDetailConvert.valueObject2Entity(orderCreateVO);
    orderDetail.setStatus(OrderStatusEnum.ORDER_CREATING);
    orderDetail.setDate(LocalDateTime.now());
    this.save(orderDetail);
    //  2.构建dto对象发送消息
    OrderMessageDTO orderMessageDTO = orderDetailConvert.entity2DataTransferObject(orderDetail);
    //  3.获取connection
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("localhost");
    //  4.获取channel
    try (Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel()) {
      String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
      //  5.发送消息给商家队列
      channel.basicPublish(
          "exchange.order.restaurant", "key.restaurant", null, messageToSend.getBytes());
    }
  }
}
