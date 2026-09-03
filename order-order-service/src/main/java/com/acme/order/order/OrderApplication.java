package com.acme.order.order;

import com.acme.order.common.mq.MqConsumeGuard;
import com.acme.order.common.redis.IdempotencyTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

/**
 * 订单服务启动入口及基础组件配置。
 *
 * @author heyu
 * @since 2026-08-12
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    IdempotencyTokenService tokenService(StringRedisTemplate r) {
        return new IdempotencyTokenService(r);
    }

    @Bean
    MqConsumeGuard guard(JdbcTemplate j) {
        return new MqConsumeGuard(j);
    }

    @Bean("productClient")
    RestClient productClient(@Value("${service-url.product}") String u) {
        return RestClient.builder().baseUrl(u).build();
    }

    @Bean("inventoryClient")
    RestClient inventoryClient(@Value("${service-url.inventory}") String u) {
        return RestClient.builder().baseUrl(u).build();
    }

    @Bean("paymentClient")
    RestClient paymentClient(@Value("${service-url.payment}") String u) {
        return RestClient.builder().baseUrl(u).build();
    }
}
