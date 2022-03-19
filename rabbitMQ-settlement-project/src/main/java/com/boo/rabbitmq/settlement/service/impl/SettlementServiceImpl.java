package com.boo.rabbitmq.settlement.service.impl;

import java.math.BigDecimal;
import java.util.Random;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.rabbitmq.settlement.entity.Settlement;
import com.boo.rabbitmq.settlement.mapper.SettlementMapper;
import com.boo.rabbitmq.settlement.service.SettlementService;

@Service
public class SettlementServiceImpl extends ServiceImpl<SettlementMapper, Settlement>
    implements SettlementService {

  Random rand = new Random(25);
  /**
   * 结算
   *
   * @param accountId 帐户id
   * @param price 价格
   * @return {@link Integer}
   */
  @Override
  public Integer settlement(Integer accountId, BigDecimal price) {
    return rand.nextInt(1000000000);
  }
}
