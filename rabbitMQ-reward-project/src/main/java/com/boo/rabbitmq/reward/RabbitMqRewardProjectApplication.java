package com.boo.rabbitmq.reward;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan("com.boo")
@MapperScan(value = "com.boo", annotationClass = Mapper.class)
public class RabbitMqRewardProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqRewardProjectApplication.class, args);
  }
}
