package com.acme.order.order;

import org.junit.jupiter.api.Test;

import com.acme.order.common.core.BizException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 订单状态机合法与非法流转测试。 */
class OrderStateMachineTest {

    private final OrderStateMachine m = new OrderStateMachine();

    @Test
    void legalTransitions() {
        assertTrue(m.canTransit(OrderStatus.WAIT_PAY, OrderStatus.WAIT_DELIVERY));
        assertTrue(m.canTransit(OrderStatus.WAIT_RECEIVE, OrderStatus.COMPLETED));
    }

    @Test
    void terminalStatesCannotReopen() {
        assertThrows(BizException.class, () -> m.require(OrderStatus.CANCELED, OrderStatus.WAIT_DELIVERY));
        assertThrows(BizException.class, () -> m.require(OrderStatus.COMPLETED, OrderStatus.WAIT_PAY));
    }
}
