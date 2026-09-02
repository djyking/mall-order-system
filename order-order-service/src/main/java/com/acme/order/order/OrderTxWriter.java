package com.acme.order.order;
import org.springframework.stereotype.Component;import org.springframework.transaction.annotation.Transactional;import java.util.UUID;
@Component public class OrderTxWriter {private final OrderRepository repo;public OrderTxWriter(OrderRepository r){repo=r;}@Transactional public void persist(long id,String no,long user,OrderDtos.SettlementView q){repo.create(id,no,user,q.totalAmountCent(),q.items().stream().mapToInt(OrderDtos.SettlementLine::quantity).sum(),q.items(),UUID.randomUUID().toString());}}
