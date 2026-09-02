package com.acme.order.common.core;

public final class BizException extends RuntimeException {
    private final ErrorCode code;
    public BizException(ErrorCode code, String message) { super(message); this.code = code; }
    public ErrorCode getCode() { return code; }
}
