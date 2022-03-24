package com.boo.ribbitmq.restaurant.service;

import java.io.IOException;

/**
 * 餐馆消息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface RestaurantMessageService {


  void publishHandle(byte[] body) throws IOException;
}
