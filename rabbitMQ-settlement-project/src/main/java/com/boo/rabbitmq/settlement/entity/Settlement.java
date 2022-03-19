package com.boo.rabbitmq.settlement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.boo.rabbitmq.settlement.enums.SettlementStatusEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName(value = "settlement")
public class Settlement implements Serializable {
    /**
     * 结算id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 订单id
     */
    @TableField(value = "order_id")
    private Integer orderId;

    /**
     * 交易id
     */
    @TableField(value = "transaction_id")
    private Integer transactionId;

    /**
     * 金额
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 状态
     */
    @TableField(value = "`status`")
    private SettlementStatusEnum status;

    /**
     * 时间
     */
    @TableField(value = "`date`")
    private LocalDateTime date;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_ORDER_ID = "order_id";

    public static final String COL_TRANSACTION_ID = "transaction_id";

    public static final String COL_AMOUNT = "amount";

    public static final String COL_STATUS = "status";

    public static final String COL_DATE = "date";
}