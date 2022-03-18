package com.boo.order.manager.dto;

import com.boo.order.manager.OrderStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class OrderMessageDTO {
    private Integer orderId;
    private OrderStatusEnum orderStatus;
    private BigDecimal price;
    private Integer deliverymanId;
    private Integer productId;
    private Integer accountId;
    private Integer settlementId;
    private Integer rewardId;
    private BigDecimal rewardAmount;
    private Boolean confirmed;
}
