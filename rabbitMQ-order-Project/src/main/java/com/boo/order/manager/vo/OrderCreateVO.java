package com.boo.order.manager.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderCreateVO {
  /** 帐户id */
  private Integer accountId;

  /** 地址 */
  private String address;
  /** 产品id */
  private Integer productId;
}
