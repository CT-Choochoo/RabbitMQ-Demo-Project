package com.boo.rabbitmq.deliveryman.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.rabbitmq.deliveryman.mapper.DeliverymanMapper;
import com.boo.rabbitmq.deliveryman.entity.Deliveryman;
import com.boo.rabbitmq.deliveryman.service.DeliverymanService;
@Service
public class DeliverymanServiceImpl extends ServiceImpl<DeliverymanMapper, Deliveryman> implements DeliverymanService{

}
