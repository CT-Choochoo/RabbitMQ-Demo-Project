package com.boo.order.manager;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * microservice订单管理器应用程序
 *
 * @author gaobo
 * @date 2022/03/19
 */
@SpringBootApplication
@ComponentScan("com.boo")
@MapperScan(value = "com.boo", annotationClass= Mapper.class)
public class MicroserviceOrderManagerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MicroserviceOrderManagerApplication.class, args);
  }
}
