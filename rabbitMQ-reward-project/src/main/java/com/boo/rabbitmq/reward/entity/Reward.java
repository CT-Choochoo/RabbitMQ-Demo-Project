package com.boo.rabbitmq.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.boo.rabbitmq.reward.enums.RewardStatusEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName(value = "reward")
public class Reward implements Serializable {
    /**
     * 奖励id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单id
     */
    @TableField(value = "order_id")
    private Integer orderId;

    /**
     * 积分量
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 状态
     */
    @TableField(value = "`status`")
    private RewardStatusEnum status;

    /**
     * 时间
     */
    @TableField(value = "`date`")
    private LocalDateTime date;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_ORDER_ID = "order_id";

    public static final String COL_AMOUNT = "amount";

    public static final String COL_STATUS = "status";

    public static final String COL_DATE = "date";
}