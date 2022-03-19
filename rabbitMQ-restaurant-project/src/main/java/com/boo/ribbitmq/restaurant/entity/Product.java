package com.boo.ribbitmq.restaurant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.boo.ribbitmq.restaurant.enums.ProductStatusEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName(value = "product")
public class Product implements Serializable {
    /**
     * 产品id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 单价
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 地址
     */
    @TableField(value = "restaurant_id")
    private Integer restaurantId;

    /**
     * 状态
     */
    @TableField(value = "`status`")
    private ProductStatusEnum status;

    /**
     * 时间
     */
    @TableField(value = "`date`")
    private Date date;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_NAME = "name";

    public static final String COL_PRICE = "price";

    public static final String COL_RESTAURANT_ID = "restaurant_id";

    public static final String COL_STATUS = "status";

    public static final String COL_DATE = "date";
}