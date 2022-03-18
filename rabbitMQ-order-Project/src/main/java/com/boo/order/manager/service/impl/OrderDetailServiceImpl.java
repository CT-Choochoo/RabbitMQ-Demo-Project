package com.boo.order.manager.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.order.manager.dao.OrderDetailMapper;
import com.boo.order.manager.po.OrderDetail;
import com.boo.order.manager.service.OrderDetailService;
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService{

}
