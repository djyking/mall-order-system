package com.acme.order.common.core;

/**
 * 通用 API 响应对象。
 *
 * @param <T> 响应数据类型
 * @param code 响应码
 * @param message 响应消息
 * @param data 响应数据
 * @param traceId 链路追踪标识
 * @author heyu
 * @since 2026-07-15
 */
public record ApiResponse<T>(String code, String message, T data, String traceId) {

  public static <T> ApiResponse<T> ok(T data, String traceId) {
    return new ApiResponse<>("OK", "success", data, traceId);
  }

  public static ApiResponse<Void> error(ErrorCode code, String message, String traceId) {
    return new ApiResponse<>(code.name(), message, null, traceId);
  }
}
