package com.acme.order.user;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import com.acme.order.common.security.JwtService;

/** 用户服务启动入口及 JWT 组件配置。 */
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
