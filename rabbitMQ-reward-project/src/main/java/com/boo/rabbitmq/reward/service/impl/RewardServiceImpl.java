package com.boo.rabbitmq.reward.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boo.rabbitmq.reward.entity.Reward;
import com.boo.rabbitmq.reward.mapper.RewardMapper;
import com.boo.rabbitmq.reward.service.RewardService;

@Service
public class RewardServiceImpl extends ServiceImpl<RewardMapper, Reward> implements RewardService {}
