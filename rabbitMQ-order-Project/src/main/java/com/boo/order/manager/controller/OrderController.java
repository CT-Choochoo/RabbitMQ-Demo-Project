package com.boo.order.manager.controller;

import com.boo.order.manager.service.OrderDetailService;
import com.boo.order.manager.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping("api/v1")
public class OrderController {

  @Autowired OrderDetailService orderService;

  @PostMapping("/orders")
  public void createOrder(@RequestBody OrderCreateVO orderCreateDTO)
      throws IOException, TimeoutException {
    log.info("createOrder:orderCreateDTO:{}", orderCreateDTO);
    orderService.createOrder(orderCreateDTO);
  }
}
