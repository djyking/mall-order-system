package com.acme.order.order;

/**
 * 订单生命周期状态。
 *
 * @author heyu
 * @since 2026-08-12
 */
public enum OrderStatus {

    /**
     * 待支付。
     */
    WAIT_PAY(10),
    /**
     * 待发货。
     */
    WAIT_DELIVERY(20),
    /**
     * 待收货。
     */
    WAIT_RECEIVE(30),
    /**
     * 已完成。
     */
    COMPLETED(40),
    /**
     * 已取消。
     */
    CANCELED(90);

    /**
     * 状态编码。
     */
    private final int code;

    OrderStatus(int c) {
        code = c;
    }

    public static OrderStatus of(int c) {
        for (var s : values()) {
            if (s.code == c) {
                return s;
            }
        }
        throw new IllegalArgumentException("unknown status " + c);
    }

    public int getCode() {
        return code;
    }
}
