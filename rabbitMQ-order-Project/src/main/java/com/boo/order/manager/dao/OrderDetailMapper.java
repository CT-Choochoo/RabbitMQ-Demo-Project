package com.boo.order.manager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boo.order.manager.po.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}