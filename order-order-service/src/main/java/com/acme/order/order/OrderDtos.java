package com.acme.order.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/** 订单服务接口使用的数据传输对象集合。 */
public final class OrderDtos {
  private OrderDtos() {}

  /** 下单商品项。 */
  public record Item(@Positive long skuId, @Min(1) @Max(999) int quantity) {}

  /** 订单结算请求。 */
  public record SettlementRequest(@NotEmpty List<@Valid Item> items) {}

  /** 单个商品的结算明细。 */
  public record SettlementLine(
      long skuId, String skuName, long priceCent, int quantity, long amountCent) {}

  /** 订单结算结果。 */
  public record SettlementView(List<SettlementLine> items, long totalAmountCent) {}

  /** 订单创建请求。 */
  public record CreateRequest(@NotEmpty List<@Valid Item> items) {}

  /** 订单详情视图。 */
  public record OrderView(
      String orderNo,
      long userId,
      String status,
      String payStatus,
      long totalAmountCent,
      List<SettlementLine> items,
      LocalDateTime createTime) {}

  /** 订单关联的支付信息。 */
  public record PayView(String payOrderNo, String status) {}
}
