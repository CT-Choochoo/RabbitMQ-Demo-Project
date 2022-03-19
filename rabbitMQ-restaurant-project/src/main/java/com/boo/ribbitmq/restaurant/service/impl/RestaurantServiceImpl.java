package com.boo.ribbitmq.restaurant.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.ribbitmq.restaurant.entity.Restaurant;
import com.boo.ribbitmq.restaurant.mapper.RestaurantMapper;
import com.boo.ribbitmq.restaurant.service.RestaurantService;
@Service
public class RestaurantServiceImpl extends ServiceImpl<RestaurantMapper, Restaurant> implements RestaurantService{

}
