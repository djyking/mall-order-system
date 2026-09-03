package com.acme.order.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 网关限流键解析配置。
 *
 * @author heyu
 * @since 2026-07-15
 */
@Configuration
public class GatewayRateLimitConfig {

    @Bean
    KeyResolver userKeyResolver() {
        return exchange -> {
            String user = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            String ip = exchange.getRequest().getRemoteAddress() == null ? "unknown"
                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(user == null ? ip : user);
        };
    }
}
