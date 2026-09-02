package com.acme.order.common.core;

/** 通用 API 响应对象。 */
public record ApiResponse<T>(String code, String message, T data, String traceId) {

  public static <T> ApiResponse<T> ok(T data, String traceId) {
    return new ApiResponse<>("OK", "success", data, traceId);
  }

  public static ApiResponse<Void> error(ErrorCode code, String message, String traceId) {
    return new ApiResponse<>(code.name(), message, null, traceId);
  }
}
