package com.acme.order.query;

import com.acme.order.common.mq.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

/** 订单查询服务启动入口。 */
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class QueryApplication {
  public static void main(String[] args) {
    SpringApplication.run(QueryApplication.class, args);
  }

  @Bean
  MqConsumeGuard guard(JdbcTemplate j) {
    return new MqConsumeGuard(j);
  }
}
