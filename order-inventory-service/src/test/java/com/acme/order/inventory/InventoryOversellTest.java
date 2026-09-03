package com.acme.order.inventory;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 库存并发扣减不超卖测试。
 *
 * @author heyu
 * @since 2026-08-02
 */
class InventoryOversellTest {

    @Test
    void conditionalUpdatePreventsOversell() throws Exception {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:stock;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(ds);
        jdbc.execute(
            "CREATE TABLE inventory_stock(sku_id BIGINT PRIMARY KEY,available_stock INT,"
                + "reserved_stock INT,version INT)");
        jdbc.update("INSERT INTO inventory_stock VALUES(10001,10,0,0)");
        var pool = Executors.newFixedThreadPool(32);
        var tasks = IntStream.range(0, 100).mapToObj(i -> (Callable<Integer>) () -> jdbc.update(
            "UPDATE inventory_stock SET available_stock=available_stock-1,reserved_stock=reserved_stock+1,"
                + "version=version+1 WHERE sku_id=10001 AND available_stock>=1"))
            .toList();
        int success = 0;
        for (var f : pool.invokeAll(tasks)) {
            success += f.get();
        }
        pool.shutdown();
        var row = jdbc.queryForMap("SELECT available_stock,reserved_stock FROM inventory_stock WHERE sku_id=10001");
        Assertions.assertEquals(10, success);
        Assertions.assertEquals(0, ((Number) row.get("AVAILABLE_STOCK")).intValue());
        Assertions.assertEquals(10, ((Number) row.get("RESERVED_STOCK")).intValue());
    }
}
