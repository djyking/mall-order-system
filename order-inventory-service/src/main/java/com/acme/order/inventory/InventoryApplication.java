package com.acme.order.inventory;

import com.acme.order.common.mq.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

/** 库存服务启动入口及消息消费组件配置。 */
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class InventoryApplication {
  public static void main(String[] args) {
    SpringApplication.run(InventoryApplication.class, args);
  }

  @Bean
  MqConsumeGuard mqConsumeGuard(JdbcTemplate j) {
    return new MqConsumeGuard(j);
  }
}
