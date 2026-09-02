package com.acme.order.order;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 在订单事务中写入订单聚合及领域事件。 */
@Component
public class OrderTxWriter {
  private final OrderRepository repo;

  public OrderTxWriter(OrderRepository r) {
    repo = r;
  }

  @Transactional
  public void persist(long id, String no, long user, OrderDtos.SettlementView q) {
    repo.create(
        id,
        no,
        user,
        q.totalAmountCent(),
        q.items().stream().mapToInt(OrderDtos.SettlementLine::quantity).sum(),
        q.items(),
        UUID.randomUUID().toString());
  }
}
