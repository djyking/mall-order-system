package com.acme.order.order;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

/**
 * 订单服务接口使用的数据传输对象集合。
 *
 * @author heyu
 * @since 2026-08-12
 */
public final class OrderDtos {

    private OrderDtos() {
    }

    /**
     * 下单商品项。
     *
     * @param skuId SKU 标识
     * @param quantity 数量
     * @author heyu
     * @since 2026-08-12
     */
    public record Item(@Positive long skuId, @Min(1) @Max(999) int quantity) {
    }

    /**
     * 订单结算请求。
     *
     * @param items 商品明细列表
     * @author heyu
     * @since 2026-08-12
     */
    public record SettlementRequest(@NotEmpty List<@Valid Item> items) {
    }

    /**
     * 单个商品的结算明细。
     *
     * @param skuId SKU 标识
     * @param skuName SKU 名称
     * @param priceCent 价格，单位为分
     * @param quantity 数量
     * @param amountCent 金额，单位为分
     * @author heyu
     * @since 2026-08-12
     */
    public record SettlementLine(long skuId, String skuName, long priceCent, int quantity, long amountCent) {
    }

    /**
     * 订单结算结果。
     *
     * @param items 商品明细列表
     * @param totalAmountCent 总金额，单位为分
     * @author heyu
     * @since 2026-08-12
     */
    public record SettlementView(List<SettlementLine> items, long totalAmountCent) {
    }

    /**
     * 订单创建请求。
     *
     * @param items 商品明细列表
     * @author heyu
     * @since 2026-08-12
     */
    public record CreateRequest(@NotEmpty List<@Valid Item> items) {
    }

    /**
     * 订单详情视图。
     *
     * @param orderNo 订单号
     * @param userId 用户标识
     * @param status 状态
     * @param payStatus 支付状态
     * @param totalAmountCent 总金额，单位为分
     * @param items 商品明细列表
     * @param createTime 创建时间
     * @author heyu
     * @since 2026-08-12
     */
    public record OrderView(String orderNo, long userId, String status, String payStatus, long totalAmountCent,
        List<SettlementLine> items, LocalDateTime createTime) {
    }

    /**
     * 订单关联的支付信息。
     *
     * @param payOrderNo 支付单号
     * @param status 状态
     * @author heyu
     * @since 2026-08-12
     */
    public record PayView(String payOrderNo, String status) {
    }
}
