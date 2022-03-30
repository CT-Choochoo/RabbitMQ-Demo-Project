package com.boo.rabbitmq.settlement;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.boo")
@MapperScan(value = "com.boo", annotationClass = Mapper.class)
public class RabbitMqSettlementProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqSettlementProjectApplication.class, args);
  }
}
