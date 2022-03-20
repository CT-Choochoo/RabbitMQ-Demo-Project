package com.boo.order.manager.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.boo.order.manager.enums.OrderStatusEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

@TableName(value = "order_detail")
@Data
public class OrderDetail implements Serializable {

  /**
   * 订单id
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  /**
   * 状态
   */
  @TableField(value = "`status`")
  private OrderStatusEnum status;

  /**
   * 订单地址
   */
  @TableField(value = "address")
  private String address;

  /**
   * 用户id
   */
  @TableField(value = "account_id")
  private Integer accountId;

  /**
   * 产品id
   */
  @TableField(value = "product_id")
  private Integer productId;

  /**
   * 骑手id
   */
  @TableField(value = "deliveryman_id")
  private Integer deliverymanId;

  /**
   * 结算id
   */
  @TableField(value = "settlement_id")
  private Integer settlementId;

  /**
   * 积分奖励id
   */
  @TableField(value = "reward_id")
  private Integer rewardId;

  /**
   * 价格
   */
  @TableField(value = "price")
  private BigDecimal price;

  /**
   * 时间
   */
  @TableField(value = "`date`")
  private LocalDateTime date;

  private static final long serialVersionUID = 1L;

  public static final String COL_ID = "id";

  public static final String COL_STATUS = "status";

  public static final String COL_ADDRESS = "address";

  public static final String COL_ACCOUNT_ID = "account_id";

  public static final String COL_PRODUCT_ID = "product_id";

  public static final String COL_DELIVERYMAN_ID = "deliveryman_id";

  public static final String COL_SETTLEMENT_ID = "settlement_id";

  public static final String COL_REWARD_ID = "reward_id";

  public static final String COL_PRICE = "price";

  public static final String COL_DATE = "date";
}