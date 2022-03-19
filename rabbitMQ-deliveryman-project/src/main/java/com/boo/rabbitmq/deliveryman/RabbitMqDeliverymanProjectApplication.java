package com.boo.rabbitmq.deliveryman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * rabbitmq送货人工程应用
 *
 * @author gaobo
 * @date 2022/03/19
 */
@EnableAsync
@SpringBootApplication
public class RabbitMqDeliverymanProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(RabbitMqDeliverymanProjectApplication.class, args);
  }
}
