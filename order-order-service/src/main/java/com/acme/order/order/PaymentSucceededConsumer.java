package com.acme.order.order;

import com.acme.order.common.mq.MqConsumeGuard;
import com.acme.order.common.mq.RabbitTopology;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消费支付成功事件并推进订单状态。
 *
 * @author heyu
 * @since 2026-08-12
 */
@Component
public class PaymentSucceededConsumer {

    private final MqConsumeGuard guard;
    private final OrderService service;

    public PaymentSucceededConsumer(MqConsumeGuard g, OrderService s) {
        guard = g;
        service = s;
    }

    @Transactional
    @RabbitListener(queues = RabbitTopology.PAYMENT_QUEUE)
    public void consume(JsonNode event) {
        String id = event.path("eventId").asText();
        String no = event.path("payload").path("orderNo").asText();
        long user = event.path("payload").path("userId").asLong();
        if (!guard.first("order-payment", id)) {
            return;
        }
        service.paymentSucceeded(no, user);
        guard.success("order-payment", id);
    }
}
