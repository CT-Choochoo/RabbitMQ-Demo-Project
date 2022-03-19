package com.boo.order.manager.dto;

import com.boo.order.manager.enums.OrderStatusEnum;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderMessageDTO {
  /** 订单id */
  private Integer orderId;

  /** 订单状态 */
  private OrderStatusEnum orderStatus;

  /** 价格 */
  private BigDecimal price;
  /** 送货人id */
  private Integer deliverymanId;
  /** 产品id */
  private Integer productId;
  /** 帐户id */
  private Integer accountId;
  /** 结算id */
  private Integer settlementId;
  /** 奖励id */
  private Integer rewardId;
  /** 奖励金额 */
  private BigDecimal rewardAmount;
  /** 确认 */
  private Boolean confirmed;
}
