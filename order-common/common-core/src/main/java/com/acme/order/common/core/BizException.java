package com.acme.order.common.core;

/** 携带业务错误码的运行时异常。 */
public final class BizException extends RuntimeException {
  private final ErrorCode code;

  public BizException(ErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public ErrorCode getCode() {
    return code;
  }
}
