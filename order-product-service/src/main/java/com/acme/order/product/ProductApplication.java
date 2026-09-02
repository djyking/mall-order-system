package com.acme.order.product;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

/** 商品服务启动入口。 */
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
