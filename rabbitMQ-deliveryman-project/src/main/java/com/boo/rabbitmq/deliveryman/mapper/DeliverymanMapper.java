package com.boo.rabbitmq.deliveryman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boo.rabbitmq.deliveryman.entity.Deliveryman;
import org.apache.ibatis.annotations.Mapper;

/**
 * 送货人映射器
 *
 * @author gaobo
 * @date 2022/03/19
 */@Mapper
public interface DeliverymanMapper extends BaseMapper<Deliveryman> {
}