package com.acme.order.user;

import java.time.Duration;

import com.acme.order.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 用户服务启动入口及 JWT 组件配置。
 *
 * @author heyu
 * @since 2026-07-24
 */
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Bean
    JwtService jwtService(@Value("${security.jwt.secret}") String secret) {
        return new JwtService(secret, Duration.ofHours(8));
    }
}
