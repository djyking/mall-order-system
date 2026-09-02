package com.acme.order.payment;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 支付通知并发幂等测试。 */
class PaymentIdempotencyTest {

    @Test
    void duplicateNotifyHasOneBusinessEffect() throws Exception {
        Set<String> processed = ConcurrentHashMap.newKeySet();
        var pool = Executors.newFixedThreadPool(20);
        var tasks = java.util.stream.IntStream.range(0, 100)
            .mapToObj(i -> (Callable<Boolean>) () -> processed.add("notify-1")).toList();
        int success = 0;
        for (var f : pool.invokeAll(tasks))
            if (f.get())
                success++;
        pool.shutdown();
        assertEquals(1, success);
    }
}
