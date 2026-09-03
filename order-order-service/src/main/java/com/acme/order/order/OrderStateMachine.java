package com.acme.order.order;

import java.util.Map;
import java.util.Set;

import com.acme.order.common.core.BizException;
import com.acme.order.common.core.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 校验订单状态流转是否合法。
 *
 * @author heyu
 * @since 2026-08-12
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> RULES = Map.of(OrderStatus.WAIT_PAY,
        Set.of(OrderStatus.WAIT_DELIVERY, OrderStatus.CANCELED), OrderStatus.WAIT_DELIVERY,
        Set.of(OrderStatus.WAIT_RECEIVE), OrderStatus.WAIT_RECEIVE, Set.of(OrderStatus.COMPLETED));

    public boolean canTransit(OrderStatus from, OrderStatus to) {
        return RULES.getOrDefault(from, Set.of()).contains(to);
    }

    public void require(OrderStatus from, OrderStatus to) {
        if (!canTransit(from, to)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "非法订单状态流转: " + from + " -> " + to);
        }
    }
}
