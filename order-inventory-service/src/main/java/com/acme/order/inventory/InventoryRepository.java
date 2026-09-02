package com.acme.order.inventory;

import com.acme.order.api.inventory.InventoryDtos.StockView;
import com.acme.order.common.core.*;
import java.time.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 库存与库存预占记录的数据访问组件。 */
@Repository
public class InventoryRepository {
  private final JdbcTemplate jdbc;

  public InventoryRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public StockView stock(long sku) {
    return jdbc.query(
        "SELECT total_stock,available_stock,reserved_stock,sold_stock FROM inventory_stock WHERE sku_id=?",
        r -> {
          if (!r.next())
            throw new BizException(ErrorCode.INVENTORY_RESERVATION_NOT_FOUND, "库存记录不存在");
          return new StockView(sku, r.getInt(1), r.getInt(2), r.getInt(3), r.getInt(4));
        },
        sku);
  }

  public void reserve(String orderNo, long userId, long sku, int qty) {
    int changed =
        jdbc.update(
            "UPDATE inventory_stock SET available_stock=available_stock-?,reserved_stock=reserved_stock+?,version=version+1,update_time=NOW(3) WHERE sku_id=? AND available_stock>=?",
            qty,
            qty,
            sku,
            qty);
    if (changed != 1) throw new BizException(ErrorCode.INVENTORY_NOT_ENOUGH, "库存不足");
    long id = Ids.next();
    jdbc.update(
        "INSERT INTO inventory_reservation(id,reservation_no,order_no,user_id,sku_id,quantity,status,expire_time,version,create_time,update_time) VALUES(?,?,?,?,?,?,10,?,0,NOW(3),NOW(3))",
        id,
        "R" + id,
        orderNo,
        userId,
        sku,
        qty,
        LocalDateTime.now().plusMinutes(35));
    jdbc.update(
        "INSERT INTO inventory_change_log(id,sku_id,order_no,reservation_no,operation_type,quantity,create_time) VALUES(?,?,?,?,?,?,NOW(3))",
        Ids.next(),
        sku,
        orderNo,
        "R" + id,
        "RESERVE",
        qty);
  }

  public int confirm(String orderNo) {
    return change(orderNo, 20, "CONFIRM", -1);
  }

  public int release(String orderNo) {
    return change(orderNo, 30, "RELEASE", 1);
  }

  private int change(String orderNo, int target, String op, int availableSign) {
    var rows =
        jdbc.query(
            "SELECT sku_id,quantity FROM inventory_reservation WHERE order_no=? AND status=10 FOR UPDATE",
            (r, n) -> new long[] {r.getLong(1), r.getInt(2)},
            orderNo);
    int count = 0;
    for (var row : rows) {
      long sku = row[0];
      int qty = (int) row[1];
      int changed =
          jdbc.update(
              "UPDATE inventory_reservation SET status=?,version=version+1,update_time=NOW(3) WHERE order_no=? AND sku_id=? AND status=10",
              target,
              orderNo,
              sku);
      if (changed == 1) {
        if (availableSign > 0)
          jdbc.update(
              "UPDATE inventory_stock SET available_stock=available_stock+?,reserved_stock=reserved_stock-?,version=version+1 WHERE sku_id=? AND reserved_stock>=?",
              qty,
              qty,
              sku,
              qty);
        else
          jdbc.update(
              "UPDATE inventory_stock SET reserved_stock=reserved_stock-?,sold_stock=sold_stock+?,version=version+1 WHERE sku_id=? AND reserved_stock>=?",
              qty,
              qty,
              sku,
              qty);
        jdbc.update(
            "INSERT INTO inventory_change_log(id,sku_id,order_no,operation_type,quantity,create_time) VALUES(?,?,?,?,?,NOW(3))",
            Ids.next(),
            sku,
            orderNo,
            op,
            qty);
        count++;
      }
    }
    return count;
  }
}
