package com.acme.order.common.core;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务主键和单号生成工具。
 *
 * @author heyu
 * @since 2026-07-15
 */
public final class Ids {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private Ids() {
    }

    public static long next() {
        return Instant.now().toEpochMilli() * 1_000 + SEQUENCE.getAndIncrement() % 1_000;
    }

    public static String orderNo(long id) {
        return "O" + id;
    }

    public static String payNo(long id) {
        return "P" + id;
    }
}
