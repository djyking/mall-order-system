package com.acme.order.payment;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.acme.order.common.mq.RabbitTopology;

/** 定时发布支付本地消息表中的待发送事件。 */
@Component
public class PaymentOutboxPublisher {

    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    public PaymentOutboxPublisher(JdbcTemplate j, RabbitTemplate r, ObjectMapper m) {
        jdbc = j;
        rabbit = r;
        mapper = m;
    }

    @Scheduled(fixedDelayString = "${outbox.delay-ms:1000}")
    public void run() {
        var list = jdbc.query(
            "SELECT id,event_id,aggregate_id,payload FROM outbox_event WHERE event_status IN(0,20) AND next_retry_time<=NOW(3) ORDER BY id LIMIT 100",
            (x, n) -> Map.of("id", x.getLong(1), "event", x.getString(2), "aggregate", x.getString(3), "payload",
                x.getString(4)));
        for (var e : list)
            try {
                if (jdbc.update(
                    "UPDATE outbox_event SET event_status=5,update_time=NOW(3) WHERE id=? AND event_status IN(0,20)",
                    e.get("id")) != 1)
                    continue;
                var correlation = new CorrelationData((String) e.get("event"));
                rabbit.convertAndSend(RabbitTopology.EXCHANGE, "payment.succeeded",
                    Map.of("eventId", e.get("event"), "eventType", "PaymentSucceeded", "eventVersion", 1, "occurredAt",
                        OffsetDateTime.now().toString(), "producer", "order-payment-service", "aggregateId",
                        e.get("aggregate"), "payload", mapper.readTree((String) e.get("payload"))),
                    correlation);
                var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
                if (!confirm.isAck())
                    throw new IllegalStateException(confirm.getReason());
                jdbc.update(
                    "UPDATE outbox_event SET event_status=10,published_time=NOW(3),update_time=NOW(3) WHERE id=?",
                    e.get("id"));
            } catch (Exception ex) {
                jdbc.update(
                    "UPDATE outbox_event SET event_status=20,retry_count=retry_count+1,next_retry_time=DATE_ADD(NOW(),INTERVAL 10 SECOND),last_error=?,update_time=NOW(3) WHERE id=?",
                    ex.getMessage(), e.get("id"));
            }
    }
}
