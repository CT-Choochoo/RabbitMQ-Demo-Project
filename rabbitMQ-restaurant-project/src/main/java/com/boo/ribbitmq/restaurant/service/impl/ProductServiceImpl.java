package com.boo.ribbitmq.restaurant.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.ribbitmq.restaurant.entity.Product;
import com.boo.ribbitmq.restaurant.mapper.ProductMapper;
import com.boo.ribbitmq.restaurant.service.ProductService;
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService{

}
