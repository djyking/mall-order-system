package com.acme.order.query;

import java.util.Map;

import com.acme.order.common.mq.MqConsumeGuard;
import com.acme.order.common.mq.RabbitTopology;
import com.acme.order.common.observability.OrderMetrics;
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
    private final OrderMetrics metrics;

    public OrderProjectionConsumer(JdbcTemplate j, MqConsumeGuard g, OrderMetrics metrics) {
        jdbc = j;
        guard = g;
        this.metrics = metrics;
    }

    @Transactional
    @RabbitListener(queues = RabbitTopology.QUERY_QUEUE)
    public void consume(Map<String, Object> e) {
        try {
            consumeInTransaction(e);
        } catch (RuntimeException ex) {
            metrics.increment("mq_consume_failure_total", "consumer", "query-projection");
            throw ex;
        }
    }

    private void consumeInTransaction(Map<String, Object> e) {
        String id = String.valueOf(e.get("eventId"));
        String type = String.valueOf(e.get("eventType"));
        Map<?, ?> payload = (Map<?, ?>) e.get("payload");
        String no = String.valueOf(payload.get("orderNo"));
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
        long user = number(payload.get("userId"));
        long amount = number(payload.get("totalAmountCent"));
        long payAmount = "OrderPaid".equals(type) ? amount : 0;
        int itemCount = (int) number(payload.get("itemCount"));
        int skuCount = (int) number(payload.get("skuCount"));
        String firstItemName = payload.get("firstItemName") == null ? null : String.valueOf(payload.get("firstItemName"));
        boolean paid = "OrderPaid".equals(type);
        boolean canceled = "OrderCanceled".equals(type);
        int eventVersion = e.get("eventVersion") instanceof Number version ? version.intValue() : 1;
        jdbc.update(
            "INSERT INTO order_query_projection(order_no,user_id,status,pay_status,total_amount_cent,pay_amount_cent,"
                + "item_count,sku_count,first_item_name,event_version,pay_time,cancel_time,create_time,update_time) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,IF(?,NOW(3),NULL),IF(?,NOW(3),NULL),NOW(3),NOW(3)) "
                + "ON DUPLICATE KEY UPDATE "
                + "status=IF(VALUES(event_version)>=event_version,VALUES(status),status),"
                + "pay_status=IF(VALUES(event_version)>=event_version AND VALUES(pay_status)='PAID','PAID',pay_status),"
                + "pay_amount_cent=IF(VALUES(event_version)>=event_version,"
                + "GREATEST(pay_amount_cent,VALUES(pay_amount_cent)),pay_amount_cent),"
                + "user_id=IF(VALUES(event_version)>=event_version AND VALUES(user_id)<>0,VALUES(user_id),user_id),"
                + "total_amount_cent=IF(VALUES(event_version)>=event_version AND VALUES(total_amount_cent)<>0,"
                + "VALUES(total_amount_cent),total_amount_cent),"
                + "item_count=IF(VALUES(event_version)>=event_version AND VALUES(item_count)<>0,"
                + "VALUES(item_count),item_count),"
                + "sku_count=IF(VALUES(event_version)>=event_version AND VALUES(sku_count)<>0,"
                + "VALUES(sku_count),sku_count),"
                + "first_item_name=IF(VALUES(event_version)>=event_version,"
                + "COALESCE(VALUES(first_item_name),first_item_name),first_item_name),"
                + "pay_time=IF(VALUES(event_version)>=event_version,COALESCE(VALUES(pay_time),pay_time),pay_time),"
                + "cancel_time=IF(VALUES(event_version)>=event_version,"
                + "COALESCE(VALUES(cancel_time),cancel_time),cancel_time),"
                + "event_version=GREATEST(event_version,VALUES(event_version)),update_time=NOW(3)",
            no, user, status, paid ? "PAID" : "UNPAID", amount, payAmount, itemCount, skuCount, firstItemName,
            eventVersion, paid, canceled);
        guard.success("query-projection", id);
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }
}
