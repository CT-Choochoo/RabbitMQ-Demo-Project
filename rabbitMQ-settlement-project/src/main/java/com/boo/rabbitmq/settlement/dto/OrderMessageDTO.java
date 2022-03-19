package com.boo.rabbitmq.settlement.dto;

import com.imooc.food.settlementservicemanager.enummeration.OrderStatus;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderMessageDTO {
    private Integer orderId;
    private OrderStatus orderStatus;
    private BigDecimal price;
    private Integer deliverymanId;
    private Integer productId;
    private Integer accountId;
    private Integer settlementId;
    private Integer rewardId;
    private BigDecimal rewardAmount;
    private Boolean confirmed;
}
