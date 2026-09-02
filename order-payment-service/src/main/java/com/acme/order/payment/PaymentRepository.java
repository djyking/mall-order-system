package com.acme.order.payment;

import com.acme.order.api.payment.PaymentDtos.PayView;
import com.acme.order.common.core.*;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 支付单、支付通知和支付事件的数据访问组件。 */
@Repository
public class PaymentRepository {
  private final JdbcTemplate jdbc;

  public PaymentRepository(JdbcTemplate j) {
    jdbc = j;
  }

  public PayView create(String order, long user, long amount) {
    try {
      long id = Ids.next();
      String no = Ids.payNo(id);
      jdbc.update(
          "INSERT INTO pay_order(id,pay_order_no,biz_order_no,user_id,amount_cent,status,channel_code,version,create_time,update_time) VALUES(?,?,?,?,?,10,'MOCK',0,NOW(3),NOW(3))",
          id,
          no,
          order,
          user,
          amount);
      return new PayView(no, order, amount, "UNPAID");
    } catch (DuplicateKeyException e) {
      return byOrder(order);
    }
  }

  public PayView get(String no) {
    return jdbc.query(
        "SELECT pay_order_no,biz_order_no,amount_cent,status FROM pay_order WHERE pay_order_no=?",
        r -> {
          if (!r.next()) throw new BizException(ErrorCode.PAY_ORDER_NOT_FOUND, "支付单不存在");
          return new PayView(
              r.getString(1), r.getString(2), r.getLong(3), r.getInt(4) == 20 ? "PAID" : "UNPAID");
        },
        no);
  }

  public PayView byOrder(String order) {
    return jdbc.query(
        "SELECT pay_order_no,biz_order_no,amount_cent,status FROM pay_order WHERE biz_order_no=?",
        r -> {
          if (!r.next()) throw new BizException(ErrorCode.PAY_ORDER_NOT_FOUND, "支付单不存在");
          return new PayView(
              r.getString(1), r.getString(2), r.getLong(3), r.getInt(4) == 20 ? "PAID" : "UNPAID");
        },
        order);
  }

  public boolean success(String no, String notifyId) {
    var p = get(no);
    long userId =
        jdbc.queryForObject("SELECT user_id FROM pay_order WHERE pay_order_no=?", Long.class, no);
    try {
      jdbc.update(
          "INSERT INTO pay_notify_log(id,notify_id,pay_order_no,channel_code,notify_payload,process_status,create_time,update_time) VALUES(?,?,?,'MOCK','{}',0,NOW(3),NOW(3))",
          Ids.next(),
          notifyId,
          no);
    } catch (DuplicateKeyException e) {
      return false;
    }
    int n =
        jdbc.update(
            "UPDATE pay_order SET status=20,pay_time=NOW(3),version=version+1,update_time=NOW(3) WHERE pay_order_no=? AND status=10",
            no);
    if (n == 1) {
      String event = UUID.randomUUID().toString();
      String payload =
          "{\"orderNo\":\""
              + p.orderNo()
              + "\",\"payOrderNo\":\""
              + no
              + "\",\"userId\":"
              + userId
              + "}";
      jdbc.update(
          "INSERT INTO outbox_event(id,event_id,event_type,aggregate_type,aggregate_id,payload,event_status,retry_count,next_retry_time,create_time,update_time) VALUES(?,?,'PaymentSucceeded','PAYMENT',?,?,0,0,NOW(3),NOW(3),NOW(3))",
          Ids.next(),
          event,
          no,
          payload);
      jdbc.update(
          "UPDATE pay_notify_log SET process_status=1,update_time=NOW(3) WHERE notify_id=?",
          notifyId);
      return true;
    }
    return false;
  }
}
