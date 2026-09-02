package com.acme.order.payment;
import com.acme.order.common.mq.RabbitTopology;import org.springframework.boot.*;import org.springframework.boot.autoconfigure.*;import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling @SpringBootApplication(scanBasePackages="com.acme.order") public class PaymentApplication {public static void main(String[] args){SpringApplication.run(PaymentApplication.class,args);}}
