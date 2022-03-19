package com.boo.rabbitmq.settlement.service;

import com.boo.rabbitmq.settlement.entity.Settlement;
import com.baomidou.mybatisplus.extension.service.IService;
import java.math.BigDecimal;

/**
 * 结算服务
 *
 * @author gaobo
 * @date 2022/03/19
 */
public interface SettlementService extends IService<Settlement> {

  /**
   * 结算
   *
   * @param accountId 帐户id
   * @param price 价格
   * @return {@link Integer}
   */
  Integer settlement(Integer accountId, BigDecimal price);
}
