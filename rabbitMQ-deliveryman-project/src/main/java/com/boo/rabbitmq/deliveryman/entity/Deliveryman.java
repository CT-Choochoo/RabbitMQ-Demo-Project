package com.boo.rabbitmq.deliveryman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 送货人
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Data
@TableName(value = "deliveryman")
public class Deliveryman implements Serializable {
  /** 骑手id */
  @TableId(value = "id", type = IdType.INPUT)
  private Integer id;

  /** 名称 */
  @TableField(value = "`name`")
  private String name;

  /** 状态 */
  @TableField(value = "`status`")
  private String status;

  /** 时间 */
  @TableField(value = "`date`")
  private Date date;

  private static final long serialVersionUID = 1L;

  public static final String COL_ID = "id";

  public static final String COL_NAME = "name";

  public static final String COL_STATUS = "status";

  public static final String COL_DATE = "date";
}
