package com.boo.order.manager.controller;

import com.boo.order.manager.service.OrderDetailService;
import com.boo.order.manager.vo.OrderCreateVO;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

  @PostMapping("/order")
  public String createOrder(@RequestBody OrderCreateVO orderCreateDTO)
      throws IOException, TimeoutException, InterruptedException {
    log.info("createOrder:orderCreateDTO:{}", orderCreateDTO);
    orderService.createOrder(orderCreateDTO);
    return "Success!";
  }

  @PostMapping("orderList")
  public String createOrderList(@RequestBody OrderCreateVO vo)
      throws IOException, TimeoutException, InterruptedException {
    log.info("createOrder:orderCreateDTO:{}", vo);
    List<OrderCreateVO> list = new ArrayList<>(50);
    for (int i = 0; i < 50; i++) {
      final OrderCreateVO orderCreateVO = new OrderCreateVO();
      BeanUtils.copyProperties(vo,orderCreateVO);
      list.add(orderCreateVO);
    }
    orderService.createOrderList(list);

    return "Success!";
  }
}
