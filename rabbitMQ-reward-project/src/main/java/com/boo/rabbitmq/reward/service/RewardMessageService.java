package com.boo.rabbitmq.reward.service;

/**
 * 奖励消息服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface RewardMessageService {

  void handleMessage() throws InterruptedException;
}
