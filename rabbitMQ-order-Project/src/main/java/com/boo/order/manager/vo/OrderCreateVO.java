package com.boo.order.manager.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderCreateVO {
    private Integer accountId;
    private String address;
    private Integer productId;
}
