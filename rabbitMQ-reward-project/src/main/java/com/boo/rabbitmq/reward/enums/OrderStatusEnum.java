package com.boo.rabbitmq.reward.enums;

/**
 * 订单状态枚举
 *
 * @author gaobo
 * @date 2022/03/19
 */
public enum OrderStatusEnum {
  /** 订单创建 */
  ORDER_CREATING,
  /** 餐厅确认 */
  RESTAURANT_CONFIRMED,
  /** 送货人确认 */
  DELIVERYMAN_CONFIRMED,
  /** 结算确认 */
  SETTLEMENT_CONFIRMED,
  /** 订单结算 */
  ORDER_CREATED,
  /** 失败 */
  FAILED;
}
