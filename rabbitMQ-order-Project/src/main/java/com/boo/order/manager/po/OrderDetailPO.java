package com.boo.order.manager.po;

import com.boo.order.manager.enums.OrderStatusEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import org.apache.tomcat.jni.Local;

/**
 * order_detal 表对应的Entity
 */
@Data
public class OrderDetailPO {
    private Integer id;
    private OrderStatusEnum status;
    private String address;
    private Integer accountId;
    private Integer productId;
    private Integer deliverymanId;
    private Integer settlementId;
    private Integer rewardId;
    private BigDecimal price;
    private LocalDateTime date;
}
