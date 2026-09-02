package com.acme.order.query;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.acme.order.common.mq.MqConsumeGuard;

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
