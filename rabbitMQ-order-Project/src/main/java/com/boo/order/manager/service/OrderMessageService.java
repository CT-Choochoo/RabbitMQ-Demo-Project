package com.boo.order.manager.service;

import com.boo.order.manager.dto.OrderMessageDTO;

/**
 * 订单信息服务
 *
 * @author gaobo
 * @date 2022/03/18
 */
public interface OrderMessageService {

  /**
   * 发布处理
   *
   * @param body 消息byte[]
   */
  void handleMessage(OrderMessageDTO body);
}
