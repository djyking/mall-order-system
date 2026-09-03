package com.acme.order.common.core;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 跨服务传递的领域事件信封。
 *
 * @param eventId 事件标识
 * @param eventType 事件类型
 * @param eventVersion 事件版本
 * @param occurredAt 事件发生时间
 * @param traceId 链路追踪标识
 * @param producer 事件生产者
 * @param aggregateId 聚合根标识
 * @param payload 事件载荷
 * @author heyu
 * @since 2026-07-15
 */
public record EventEnvelope(
    String eventId,
    String eventType,
    int eventVersion,
    OffsetDateTime occurredAt,
    String traceId,
    String producer,
    String aggregateId,
    Map<String, Object> payload) { }
