package com.boo.order.manager.enums;

/**
 * Rabbit组件枚举
 *
 * @author gaobo
 * @date 2022/03/26
 */
public interface RabbitComponentEnum {
  /**
   * 交换枚举
   *
   * @author gaobo
   * @date 2022/03/26
   */
  enum ExchangeEnum {
    /** 订单.餐厅 */
    ORDER_RESTAURANT("exchange.order.restaurant"),
    /** 订单.送货人 */
    ORDER_DELIVERYMAN("exchange.order.deliveryman"),
    /** 订单.结算 */
    ORDER_SETTLEMENT("exchange.order.settlement"),
    /** 订单 */
    SETTLEMENT_ORDER("exchange.settlement.order"),
    /** 结算.奖励 */
    ORDER_REWARD("exchange.order.reward"),
    /** 死信 */
    DLX("exchange.dlx");

    private String code;

    public String getCode() {
      return code;
    }

    ExchangeEnum(String code) {
      this.code = code;
    }
  }

  /**
   * 队列枚举
   *
   * @author gaobo
   * @date 2022/03/26
   */
  enum QueueEnum {
    /** 订单 */
    ORDER("queue.order"),
    /** 死信队列 */
    DLX("queue.dlx");
    private String code;

    public String getCode() {
      return code;
    }

    QueueEnum(String code) {
      this.code = code;
    }
  }

  enum RoutingKey {
    /** order路由键 */
    KEY_ORDER("key.order"),
    KEY_REWARD("key.reward"),
    KEY_SETTLEMENT("key.settlement"),
    KEY_DELIVERYMAN("key.deliveryman"),
    ALL("#");
    private String code;

    public String getCode() {
      return code;
    }

    RoutingKey(String code) {
      this.code = code;
    }
  }
}
