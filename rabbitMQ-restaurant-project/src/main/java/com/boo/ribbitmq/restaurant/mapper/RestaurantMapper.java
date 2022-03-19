package com.boo.ribbitmq.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boo.ribbitmq.restaurant.entity.Restaurant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 餐厅映射器
 *
 * @author gaobo
 * @date 2022/03/19
 */
@Mapper
public interface RestaurantMapper extends BaseMapper<Restaurant> {}
