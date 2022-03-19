package com.boo.rabbitmq.settlement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boo.rabbitmq.settlement.entity.Settlement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettlementMapper extends BaseMapper<Settlement> {
}