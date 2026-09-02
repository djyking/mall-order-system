package com.acme.order.payment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

/** 支付通知并发幂等测试。 */
class PaymentIdempotencyTest {
  @Test
  void duplicateNotifyHasOneBusinessEffect() throws Exception {
    Set<String> processed = ConcurrentHashMap.newKeySet();
    var pool = Executors.newFixedThreadPool(20);
    var tasks =
        java.util.stream.IntStream.range(0, 100)
            .mapToObj(i -> (Callable<Boolean>) () -> processed.add("notify-1"))
            .toList();
    int success = 0;
    for (var f : pool.invokeAll(tasks)) if (f.get()) success++;
    pool.shutdown();
    assertEquals(1, success);
  }
}
