package com.boo.order.manager.enums;

public enum OrderStatusEnum {
    ORDER_CREATING,
    RESTAURANT_CONFIRMED,
    /** 送货人确认 */DELIVERYMAN_CONFIRMED,
    SETTLEMENT_CONFIRMED,
    ORDER_CREATED,
    FAILED;
}