package com.acme.order.query;

import com.acme.order.common.mq.MqConsumeGuard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 订单查询服务启动入口。
 *
 * @author heyu
 * @since 2026-08-27
 */
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
