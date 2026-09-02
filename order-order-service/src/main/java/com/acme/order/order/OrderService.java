package com.acme.order.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.order.api.inventory.InventoryDtos.Line;
import com.acme.order.common.core.BizException;
import com.acme.order.common.core.ErrorCode;
import com.acme.order.common.core.Ids;
import com.acme.order.common.redis.IdempotencyTokenService;

/** 编排订单结算、创建和状态流转业务。 */
@Service
public class OrderService {

    private final RemoteClients remote;
    private final OrderRepository repo;
    private final OrderStateMachine machine;
    private final IdempotencyTokenService tokens;
    private final OrderTxWriter writer;

    public OrderService(RemoteClients r, OrderRepository p, OrderStateMachine m, IdempotencyTokenService t,
        OrderTxWriter w) {
        remote = r;
        repo = p;
        machine = m;
        tokens = t;
        writer = w;
    }

    public OrderDtos.SettlementView settle(List<OrderDtos.Item> items) {
        var lines = items.stream().map(i -> {
            var sku = remote.sku(i.skuId());
            long amount = Math.multiplyExact(sku.priceCent(), i.quantity());
            return new OrderDtos.SettlementLine(sku.skuId(), sku.skuName(), sku.priceCent(), i.quantity(), amount);
        }).toList();
        return new OrderDtos.SettlementView(lines,
            lines.stream().mapToLong(OrderDtos.SettlementLine::amountCent).sum());
    }

    public OrderDtos.OrderView create(long user, String token, List<OrderDtos.Item> requested) {
        if (!tokens.acquire(user, token))
            throw new BizException(ErrorCode.ORDER_DUPLICATE_SUBMIT, "请勿重复提交");
        long id = Ids.next();
        String no = Ids.orderNo(id);
        boolean reserved = false;
        try {
            var quote = settle(requested);
            remote.reserve(no, user, requested.stream().map(i -> new Line(i.skuId(), i.quantity())).toList());
            reserved = true;
            writer.persist(id, no, user, quote);
            tokens.complete(user, token, no);
            return view(no, user);
        } catch (Exception e) {
            if (reserved)
                try {
                    remote.release(no);
                } catch (Exception ignored) {
                }
            tokens.release(user, token);
            throw e;
        }
    }

    public OrderDtos.OrderView view(String no, long user) {
        var s = repo.get(no, user);
        return new OrderDtos.OrderView(no, user, s.status().name(), s.payStatus() == 20 ? "PAID" : "UNPAID", s.total(),
            repo.items(no), s.created());
    }

    public List<OrderDtos.OrderView> list(long user, int page, int size) {
        int safe = Math.min(Math.max(size, 1), 100);
        return repo.list(user, Math.max(page, 0), safe).stream().map(x -> view(x.no(), user)).toList();
    }

    @Transactional
    public void cancel(String no, long user, String reason) {
        var s = repo.get(no, user);
        if (s.status() == OrderStatus.CANCELED)
            return;
        machine.require(s.status(), OrderStatus.CANCELED);
        if (!repo.transit(s, OrderStatus.CANCELED, "USER_CANCEL", "OrderCanceled"))
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单状态已变化");
    }

    public OrderDtos.PayView pay(String no, long user) {
        var s = repo.get(no, user);
        if (s.status() != OrderStatus.WAIT_PAY)
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "仅待支付订单可支付");
        var p = remote.createPayment(no, user, s.total());
        return new OrderDtos.PayView(p.payOrderNo(), p.status());
    }

    @Transactional
    public void deliver(String no, long user) {
        transit(no, user, OrderStatus.WAIT_DELIVERY, OrderStatus.WAIT_RECEIVE, "DELIVER", "OrderDelivered");
    }

    @Transactional
    public void receive(String no, long user) {
        transit(no, user, OrderStatus.WAIT_RECEIVE, OrderStatus.COMPLETED, "RECEIVE", "OrderCompleted");
    }

    private void transit(String no, long user, OrderStatus expected, OrderStatus target, String op, String event) {
        var snap = repo.get(no, user);
        if (snap.status() != expected)
            machine.require(snap.status(), target);
        machine.require(snap.status(), target);
        if (!repo.transit(snap, target, op, event))
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单状态已变化");
    }

    @Transactional
    public void paymentSucceeded(String no, long user) {
        var s = repo.get(no, user);
        if (s.status() == OrderStatus.WAIT_DELIVERY)
            return;
        repo.markPaid(s);
    }

    @Transactional
    public void closeExpired() {
        for (var s : repo.expired())
            if (repo.transit(s, OrderStatus.CANCELED, "TIMEOUT_CANCEL", "OrderCanceled")) {
            }
    }
}
