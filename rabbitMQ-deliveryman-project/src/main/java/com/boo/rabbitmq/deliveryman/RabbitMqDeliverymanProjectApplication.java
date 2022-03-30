package com.boo.rabbitmq.deliveryman;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * rabbitmq送货人工程应用
 *
 * @author gaobo
 * @date 2022/03/19
 */
@SpringBootApplication
@ComponentScan("com.boo")
@MapperScan(value = "com.boo", annotationClass= Mapper.class)
public class RabbitMqDeliverymanProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqDeliverymanProjectApplication.class, args);
  }
}
