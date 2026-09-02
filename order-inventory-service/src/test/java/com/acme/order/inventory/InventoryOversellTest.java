package com.acme.order.inventory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** 库存并发扣减不超卖测试。 */
class InventoryOversellTest {
  @Test
  void conditionalUpdatePreventsOversell() throws Exception {
    var ds =
        new DriverManagerDataSource("jdbc:h2:mem:stock;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    var jdbc = new JdbcTemplate(ds);
    jdbc.execute(
        "CREATE TABLE inventory_stock(sku_id BIGINT PRIMARY KEY,available_stock INT,reserved_stock INT,version INT)");
    jdbc.update("INSERT INTO inventory_stock VALUES(10001,10,0,0)");
    var pool = Executors.newFixedThreadPool(32);
    var tasks =
        java.util.stream.IntStream.range(0, 100)
            .mapToObj(
                i ->
                    (Callable<Integer>)
                        () ->
                            jdbc.update(
                                "UPDATE inventory_stock SET available_stock=available_stock-1,reserved_stock=reserved_stock+1,version=version+1 WHERE sku_id=10001 AND available_stock>=1"))
            .toList();
    int success = 0;
    for (var f : pool.invokeAll(tasks)) success += f.get();
    pool.shutdown();
    var row =
        jdbc.queryForMap(
            "SELECT available_stock,reserved_stock FROM inventory_stock WHERE sku_id=10001");
    assertEquals(10, success);
    assertEquals(0, ((Number) row.get("AVAILABLE_STOCK")).intValue());
    assertEquals(10, ((Number) row.get("RESERVED_STOCK")).intValue());
  }
}
