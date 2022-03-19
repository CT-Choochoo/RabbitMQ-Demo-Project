package com.boo.ribbitmq.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.boo.ribbitmq.restaurant.enums.RestaurantStatusEnum;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName(value = "restaurant")
public class Restaurant implements Serializable {
    /**
     * 餐厅id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 状态
     */
    @TableField(value = "`status`")
    private RestaurantStatusEnum status;

    /**
     * 结算id
     */
    @TableField(value = "settlement_id")
    private Integer settlementId;

    /**
     * 时间
     */
    @TableField(value = "`date`")
    private Date date;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_NAME = "name";

    public static final String COL_ADDRESS = "address";

    public static final String COL_STATUS = "status";

    public static final String COL_SETTLEMENT_ID = "settlement_id";

    public static final String COL_DATE = "date";
}