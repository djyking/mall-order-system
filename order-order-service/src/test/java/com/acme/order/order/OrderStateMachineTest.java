package com.acme.order.order;

import com.acme.order.common.core.BizException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 订单状态机合法与非法流转测试。
 *
 * @author heyu
 * @since 2026-08-12
 */
class OrderStateMachineTest {

    private final OrderStateMachine m = new OrderStateMachine();

    @Test
    void legalTransitions() {
        Assertions.assertTrue(m.canTransit(OrderStatus.WAIT_PAY, OrderStatus.WAIT_DELIVERY));
        Assertions.assertTrue(m.canTransit(OrderStatus.WAIT_RECEIVE, OrderStatus.COMPLETED));
    }

    @Test
    void terminalStatesCannotReopen() {
        Assertions.assertThrows(BizException.class,
            () -> m.require(OrderStatus.CANCELED, OrderStatus.WAIT_DELIVERY));
        Assertions.assertThrows(BizException.class,
            () -> m.require(OrderStatus.COMPLETED, OrderStatus.WAIT_PAY));
    }
}
