package com.acme.order.payment;

import com.acme.order.api.payment.PaymentDtos.*;
import com.acme.order.common.core.ApiResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

/** 提供支付单创建、模拟支付和查询接口。 */
@RestController
public class PaymentController {
  private final PaymentService s;

  public PaymentController(PaymentService s) {
    this.s = s;
  }

  @PostMapping("/internal/payments")
  ApiResponse<PayView> create(@RequestBody CreateRequest r) {
    return ApiResponse.ok(s.create(r), MDC.get("traceId"));
  }

  @PostMapping("/api/payments/{no}/mock-success")
  ApiResponse<Boolean> success(
      @PathVariable String no, @RequestHeader(value = "X-Notify-Id", required = false) String id) {
    return ApiResponse.ok(
        s.mockSuccess(no, id == null ? UUID.randomUUID().toString() : id), MDC.get("traceId"));
  }

  @GetMapping("/api/payments/{no}")
  ApiResponse<PayView> get(@PathVariable String no) {
    return ApiResponse.ok(s.get(no), MDC.get("traceId"));
  }
}
