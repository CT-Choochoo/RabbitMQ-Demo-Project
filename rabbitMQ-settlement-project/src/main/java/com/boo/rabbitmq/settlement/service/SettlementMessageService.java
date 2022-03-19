package com.boo.rabbitmq.settlement.service;

/**
 * 结算信息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface SettlementMessageService {

  /** 处理消息 */
  void handleMessage() throws InterruptedException;
}
