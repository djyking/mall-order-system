package com.acme.order.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商品服务启动入口。
 *
 * @author heyu
 * @since 2026-07-24
 */
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class ProductApplication {

    protected ProductApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
