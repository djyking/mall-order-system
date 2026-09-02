package com.acme.order.order;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 扫描超时未支付订单并执行关闭处理。 */
@Component
public class OrderJobs {

    private final OrderService service;

    public OrderJobs(OrderService s) {
        service = s;
    }

    @Scheduled(fixedDelayString = "${jobs.close-expired.delay-ms:60000}")
    public void closeExpired() {
        service.closeExpired();
    }
}
