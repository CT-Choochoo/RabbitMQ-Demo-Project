package com.boo.ribbitmq.restaurant;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * ribbitmq餐厅项目申请
 *
 * @author gaobo
 * @date 2022/03/19
 */
@SpringBootApplication
@ComponentScan("com.boo")
@MapperScan(value = "com.boo", annotationClass= Mapper.class)
public class RibbitmqRestaurantProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(RibbitmqRestaurantProjectApplication.class, args);
  }
}
