package com.acme.order.payment;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 支付服务启动入口。 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.acme.order")
public class PaymentApplication {
  public static void main(String[] args) {
    SpringApplication.run(PaymentApplication.class, args);
  }
}
