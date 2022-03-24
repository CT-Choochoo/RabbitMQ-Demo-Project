package com.boo.order.manager.service;

import com.boo.order.manager.po.OrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boo.order.manager.vo.OrderCreateVO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 订单细节服务
 *
 * @author gaobo
 * @date 2022/03/18
 */
public interface OrderDetailService extends IService<OrderDetail> {

  /**
   * 创建订单
   *
   * @param orderCreateVO 订单创建签证官
   * @throws IOException IO Exception
   * @throws TimeoutException 超时异常
   * @throws InterruptedException 中断异常
   */
  void createOrder(OrderCreateVO orderCreateVO)
      throws IOException, TimeoutException, InterruptedException;

  /**
   * 创建订单列表
   *
   * @param list 列表
   * @throws IOException IOexception
   * @throws TimeoutException 超时异常
   * @throws InterruptedException 中断异常
   */
  void createOrderList(List<OrderCreateVO> list)
      throws IOException, TimeoutException, InterruptedException;
}
