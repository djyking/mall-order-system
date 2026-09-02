package com.acme.order.inventory;

import com.acme.order.api.inventory.InventoryDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 编排库存预占、确认和释放事务。 */
@Service
public class InventoryService {
  private final InventoryRepository repo;

  public InventoryService(InventoryRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public void reserve(ReserveRequest r) {
    r.items().forEach(i -> repo.reserve(r.orderNo(), r.userId(), i.skuId(), i.quantity()));
  }

  @Transactional
  public int confirm(String o) {
    return repo.confirm(o);
  }

  @Transactional
  public int release(String o) {
    return repo.release(o);
  }
}
