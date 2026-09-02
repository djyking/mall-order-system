package com.acme.order.order;

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

/** 定时发布订单本地消息表中的待发送事件。 */
@Component
public class OutboxPublisher {

    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    public OutboxPublisher(JdbcTemplate j, RabbitTemplate r, ObjectMapper m) {
        jdbc = j;
        rabbit = r;
        mapper = m;
    }

    @Scheduled(fixedDelayString = "${outbox.delay-ms:1000}")
    public void publish() {
        var rows = jdbc.query(
            "SELECT id,event_id,event_type,aggregate_id,payload FROM outbox_event WHERE event_status IN (0,20) AND next_retry_time<=NOW(3) ORDER BY id LIMIT 100",
            (x, n) -> Map.of("id", x.getLong(1), "eventId", x.getString(2), "type", x.getString(3), "aggregate",
                x.getString(4), "payload", x.getString(5)));
        for (var e : rows)
            try {
                if (jdbc.update(
                    "UPDATE outbox_event SET event_status=5,update_time=NOW(3) WHERE id=? AND event_status IN(0,20)",
                    e.get("id")) != 1)
                    continue;
                String key = switch ((String) e.get("type")) {
                    case "OrderPaid" -> "order.paid";
                    case "OrderCanceled" -> "order.canceled";
                    case "OrderDelivered" -> "order.delivered";
                    case "OrderCompleted" -> "order.completed";
                    default -> "order.created";
                };
                var envelope = Map.of("eventId", e.get("eventId"), "eventType", e.get("type"), "eventVersion", 1,
                    "occurredAt", OffsetDateTime.now().toString(), "producer", "order-order-service", "aggregateId",
                    e.get("aggregate"), "payload", mapper.readTree((String) e.get("payload")));
                var correlation = new CorrelationData((String) e.get("eventId"));
                rabbit.convertAndSend(RabbitTopology.EXCHANGE, key, envelope, correlation);
                var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
                if (!confirm.isAck())
                    throw new IllegalStateException(confirm.getReason());
                jdbc.update(
                    "UPDATE outbox_event SET event_status=10,published_time=NOW(3),update_time=NOW(3) WHERE id=?",
                    e.get("id"));
            } catch (Exception ex) {
                jdbc.update(
                    "UPDATE outbox_event SET event_status=20,retry_count=retry_count+1,next_retry_time=DATE_ADD(NOW(),INTERVAL LEAST(300,POW(2,retry_count)) SECOND),last_error=?,update_time=NOW(3) WHERE id=?",
                    ex.getMessage(), e.get("id"));
            }
    }
}
