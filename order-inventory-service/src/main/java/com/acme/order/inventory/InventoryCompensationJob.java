package com.acme.order.inventory;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 扫描过期预占记录并触发库存释放的补偿任务。 */
@Component
public class InventoryCompensationJob {
  private final JdbcTemplate jdbc;
  private final InventoryService service;

  public InventoryCompensationJob(JdbcTemplate j, InventoryService s) {
    jdbc = j;
    service = s;
  }

  @Scheduled(fixedDelayString = "${jobs.reservation-compensation.delay-ms:60000}")
  public void scan() {
    List<String> orders =
        jdbc.query(
            "SELECT DISTINCT order_no FROM inventory_reservation WHERE status=10 AND expire_time<NOW(3) ORDER BY order_no LIMIT 100",
            (r, n) -> r.getString(1));
    orders.forEach(this::release);
  }

  @Transactional
  protected void release(String order) {
    service.release(order);
  }
}
