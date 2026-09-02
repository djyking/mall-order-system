package com.acme.order.api.payment;

/** 支付服务接口使用的数据传输对象集合。 */
public final class PaymentDtos {
  private PaymentDtos() {}

  /** 支付单创建请求。 */
  public record CreateRequest(String orderNo, long userId, long amountCent) {}

  /** 支付单视图。 */
  public record PayView(String payOrderNo, String orderNo, long amountCent, String status) {}
}
