package com.boo.order.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * microservice订单管理器应用程序
 *
 * @author gaobo
 * @date 2022/03/19
 */
@EnableAsync
@SpringBootApplication
public class MicroserviceOrderManagerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MicroserviceOrderManagerApplication.class, args);
  }
}
