package com.acme.order.query;

import com.acme.order.common.mq.MqConsumeGuard;
import com.acme.order.common.mq.RabbitTopology;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消费订单事件并维护查询投影。
 *
 * @author heyu
 * @since 2026-08-27
 */
@Component
public class OrderProjectionConsumer {

    private final JdbcTemplate jdbc;
    private final MqConsumeGuard guard;

    public OrderProjectionConsumer(JdbcTemplate j, MqConsumeGuard g) {
        jdbc = j;
        guard = g;
    }

    @Transactional
    @RabbitListener(queues = RabbitTopology.QUERY_QUEUE)
    public void consume(JsonNode e) {
        String id = e.path("eventId").asText();
        String type = e.path("eventType").asText();
        String no = e.path("payload").path("orderNo").asText();
        if (!guard.first("query-projection", id)) {
            return;
        }
        String status = switch (type) {
            case "OrderCreated" -> "WAIT_PAY";
            case "OrderPaid" -> "WAIT_DELIVERY";
            case "OrderDelivered" -> "WAIT_RECEIVE";
            case "OrderCanceled" -> "CANCELED";
            case "OrderCompleted" -> "COMPLETED";
            default -> null;
        };
        if (status == null) {
            guard.success("query-projection", id);
            return;
        }
        long user = e.path("payload").path("userId").asLong();
        long amount = e.path("payload").path("totalAmountCent").asLong();
        jdbc.update(
            "INSERT INTO order_query_projection(order_no,user_id,status,pay_status,total_amount_cent,event_version,"
                + "create_time,update_time) VALUES(?,?,?,'UNPAID',?,1,NOW(3),NOW(3)) "
                + "ON DUPLICATE KEY UPDATE status=VALUES(status),"
                + "pay_status=IF(VALUES(status)='WAIT_DELIVERY','PAID',pay_status),"
                + "event_version=event_version+1,update_time=NOW(3)",
            no, user, status, amount);
        guard.success("query-projection", id);
    }
}
