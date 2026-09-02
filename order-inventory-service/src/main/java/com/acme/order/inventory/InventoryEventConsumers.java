package com.acme.order.inventory;

import com.acme.order.common.mq.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 消费订单领域事件并同步库存状态。 */
@Component
public class InventoryEventConsumers {
  private final InventoryService service;
  private final MqConsumeGuard guard;

  public InventoryEventConsumers(InventoryService s, MqConsumeGuard g) {
    service = s;
    guard = g;
  }

  @Transactional
  @RabbitListener(queues = RabbitTopology.CONFIRM_QUEUE)
  public void confirm(JsonNode e) {
    handle(e, "inventory-confirm", true);
  }

  @Transactional
  @RabbitListener(queues = RabbitTopology.RELEASE_QUEUE)
  public void release(JsonNode e) {
    handle(e, "inventory-release", false);
  }

  private void handle(JsonNode e, String group, boolean confirm) {
    String event = e.path("eventId").asText(), order = e.path("payload").path("orderNo").asText();
    if (!guard.first(group, event)) return;
    if (confirm) service.confirm(order);
    else service.release(order);
    guard.success(group, event);
  }
}
