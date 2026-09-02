package com.acme.order.common.core;

import java.time.OffsetDateTime;
import java.util.Map;

/** 跨服务传递的领域事件信封。 */
public record EventEnvelope(
    String eventId,
    String eventType,
    int eventVersion,
    OffsetDateTime occurredAt,
    String traceId,
    String producer,
    String aggregateId,
    Map<String, Object> payload) {}
