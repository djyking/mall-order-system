package com.acme.order.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.acme.order.common.core.BizException;
import com.acme.order.common.core.ErrorCode;
import com.acme.order.common.core.Ids;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 订单、订单明细和事件记录的数据访问组件。
 *
 * @author heyu
 * @since 2026-08-12
 */
@Repository
public class OrderRepository {

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate j) {
        jdbc = j;
    }

    public void create(long id, String no, long user, long total, int count, List<OrderDtos.SettlementLine> items,
        String eventId) {
        jdbc.update(
            "INSERT INTO trade_order(id,order_no,user_id,status,pay_status,total_amount_cent,pay_amount_cent,"
                + "item_count,version,create_time,update_time) VALUES(?,?,?,10,10,?,?,?,0,NOW(3),NOW(3))",
            id, no, user, total, total, count);
        for (var i : items) {
            jdbc.update(
                "INSERT INTO trade_order_item(id,order_id,order_no,user_id,spu_id,sku_id,spu_name,sku_name,"
                    + "price_cent,quantity,total_amount_cent,create_time,update_time) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,NOW(3),NOW(3))",
                Ids.next(), id, no, user, 0, i.skuId(), i.skuName(), i.skuName(), i.priceCent(), i.quantity(),
                i.amountCent());
        }
        jdbc.update(
            "INSERT INTO trade_order_status_log(id,order_id,order_no,user_id,before_status,after_status,"
                + "operate_type,create_time) VALUES(?,?,?,?,NULL,10,'CREATE',NOW(3))",
            Ids.next(), id, no, user);
        insertOutbox(eventId, "OrderCreated", no,
            "{\"orderNo\":\"" + no + "\",\"userId\":" + user + ",\"totalAmountCent\":" + total + "}");
        jdbc.update(
            "INSERT INTO order_route(order_no,order_id,user_id,db_shard,table_shard,create_time) "
                + "VALUES(?,?,?,?,?,NOW(3))",
            no, id, user, user % 2, id % 8);
    }

    public Snapshot get(String no, long user) {
        return jdbc.query(
            "SELECT id,order_no,user_id,status,pay_status,total_amount_cent,version,create_time "
                + "FROM trade_order WHERE order_no=? AND user_id=?",
            r -> {
                if (!r.next()) {
                    throw new BizException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
                }
                return new Snapshot(r.getLong(1), r.getString(2), r.getLong(3), OrderStatus.of(r.getInt(4)),
                    r.getInt(5), r.getLong(6), r.getInt(7), r.getTimestamp(8).toLocalDateTime());
            }, no, user);
    }

    public List<OrderDtos.SettlementLine> items(String no) {
        return jdbc.query(
            "SELECT sku_id,sku_name,price_cent,quantity,total_amount_cent FROM trade_order_item WHERE order_no=?",
            (r, n) -> new OrderDtos.SettlementLine(r.getLong(1), r.getString(2), r.getLong(3), r.getInt(4),
                r.getLong(5)),
            no);
    }

    public List<Snapshot> list(long user, int page, int size) {
        return jdbc.query(
            "SELECT id,order_no,user_id,status,pay_status,total_amount_cent,version,create_time "
                + "FROM trade_order WHERE user_id=? ORDER BY create_time DESC LIMIT ? OFFSET ?",
            (r, n) -> new Snapshot(r.getLong(1), r.getString(2), r.getLong(3), OrderStatus.of(r.getInt(4)), r.getInt(5),
                r.getLong(6), r.getInt(7), r.getTimestamp(8).toLocalDateTime()),
            user, size, page * size);
    }

    public boolean transit(Snapshot s, OrderStatus target, String operation, String eventType) {
        int n = jdbc.update(
            "UPDATE trade_order SET status=?,version=version+1,"
                + (target == OrderStatus.CANCELED ? "cancel_time=NOW(3)," : "")
                + "update_time=NOW(3) WHERE id=? AND status=? AND version=?",
            target.getCode(), s.id(), s.status().getCode(), s.version());
        if (n == 1) {
            jdbc.update(
                "INSERT INTO trade_order_status_log(id,order_id,order_no,user_id,before_status,after_status,"
                    + "operate_type,create_time) VALUES(?,?,?,?,?,?,?,NOW(3))",
                Ids.next(), s.id(), s.no(), s.user(), s.status().getCode(), target.getCode(), operation);
            insertOutbox(UUID.randomUUID().toString(), eventType, s.no(), "{\"orderNo\":\"" + s.no() + "\"}");
        }
        return n == 1;
    }

    public void markPaid(Snapshot s) {
        int n = jdbc.update(
            "UPDATE trade_order SET status=20,pay_status=20,pay_time=NOW(3),version=version+1,update_time=NOW(3) "
                + "WHERE id=? AND status=10 AND version=?",
            s.id(), s.version());
        if (n == 1) {
            jdbc.update(
                "INSERT INTO trade_order_status_log(id,order_id,order_no,user_id,before_status,after_status,"
                    + "operate_type,create_time) VALUES(?,?,?,?,10,20,'PAY_SUCCESS',NOW(3))",
                Ids.next(), s.id(), s.no(), s.user());
            insertOutbox(UUID.randomUUID().toString(), "OrderPaid", s.no(), "{\"orderNo\":\"" + s.no() + "\"}");
        } else {
            jdbc.update(
                "INSERT INTO reconciliation_exception(id,biz_type,biz_no,exception_type,detail,status,create_time,"
                    + "update_time) VALUES(?,?,?,?,?,0,NOW(3),NOW(3))",
                Ids.next(), "ORDER", s.no(), "PAY_SUCCESS_AFTER_ORDER_CLOSED", "支付成功事件与订单关闭竞争");
        }
    }

    public List<Snapshot> expired() {
        return jdbc.query(
            "SELECT id,order_no,user_id,status,pay_status,total_amount_cent,version,create_time "
                + "FROM trade_order WHERE status=10 AND create_time<DATE_SUB(NOW(),INTERVAL 30 MINUTE) "
                + "ORDER BY id LIMIT 100",
            (r, n) -> new Snapshot(r.getLong(1), r.getString(2), r.getLong(3), OrderStatus.of(r.getInt(4)), r.getInt(5),
                r.getLong(6), r.getInt(7), r.getTimestamp(8).toLocalDateTime()));
    }

    private void insertOutbox(String id, String type, String aggregate, String payload) {
        jdbc.update(
            "INSERT INTO outbox_event(id,event_id,event_type,aggregate_type,aggregate_id,payload,event_status,"
                + "retry_count,next_retry_time,create_time,update_time) "
                + "VALUES(?,?,?,?,?,?,0,0,NOW(3),NOW(3),NOW(3))",
            Ids.next(), id, type, "ORDER", aggregate, payload);
    }

    /**
     * 订单持久化状态快照。
     *
     * @param id 主键标识
     * @param no 业务单号
     * @param user 用户信息
     * @param status 状态
     * @param payStatus 支付状态
     * @param total 总记录数
     * @param version 版本号
     * @param created 是否新建记录
     * @author heyu
     * @since 2026-08-12
     */
    public record Snapshot(long id, String no, long user, OrderStatus status, int payStatus, long total, int version,
        LocalDateTime created) {
    }
}
