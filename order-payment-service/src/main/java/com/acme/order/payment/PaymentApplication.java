package com.acme.order.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 支付服务启动入口。
 *
 * @author heyu
 * @since 2026-08-20
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class PaymentApplication {

    protected PaymentApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
