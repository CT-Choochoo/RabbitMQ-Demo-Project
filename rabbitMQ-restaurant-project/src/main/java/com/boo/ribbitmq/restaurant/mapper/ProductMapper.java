package com.boo.ribbitmq.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boo.ribbitmq.restaurant.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}