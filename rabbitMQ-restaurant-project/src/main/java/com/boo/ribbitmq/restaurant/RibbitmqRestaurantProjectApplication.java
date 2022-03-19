package com.boo.ribbitmq.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * ribbitmq餐厅项目申请
 *
 * @author gaobo
 * @date 2022/03/19
 */
@SpringBootApplication
@EnableAsync
public class RibbitmqRestaurantProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(RibbitmqRestaurantProjectApplication.class, args);
  }
}
