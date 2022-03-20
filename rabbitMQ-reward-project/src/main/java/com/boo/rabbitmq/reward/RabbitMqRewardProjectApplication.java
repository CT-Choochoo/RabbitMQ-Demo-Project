package com.boo.rabbitmq.reward;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class RabbitMqRewardProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqRewardProjectApplication.class, args);
  }
}
