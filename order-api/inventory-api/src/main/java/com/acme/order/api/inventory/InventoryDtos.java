package com.acme.order.api.inventory;

import java.util.List;

/**
 * 库存服务接口使用的数据传输对象集合。
 *
 * @author heyu
 * @since 2026-07-15
 */
public final class InventoryDtos {

    private InventoryDtos() {
    }

    /**
     * 待处理的 SKU 及其数量。
     *
     * @param skuId SKU 标识
     * @param quantity 数量
     * @author heyu
     * @since 2026-07-15
     */
    public record Line(long skuId, int quantity) {
    }

    /**
     * 库存预占请求。
     *
     * @param orderNo 订单号
     * @param userId 用户标识
     * @param items 商品明细列表
     * @author heyu
     * @since 2026-07-15
     */
    public record ReserveRequest(String orderNo, long userId, List<Line> items) {
    }

    /**
     * 库存状态变更请求。
     *
     * @param orderNo 订单号
     * @author heyu
     * @since 2026-07-15
     */
    public record ChangeRequest(String orderNo) {
    }

    /**
     * SKU 库存视图。
     *
     * @param skuId SKU 标识
     * @param totalStock 总库存数量
     * @param availableStock 可用库存数量
     * @param reservedStock 预占库存数量
     * @param soldStock 已售库存数量
     * @author heyu
     * @since 2026-07-15
     */
    public record StockView(long skuId, int totalStock, int availableStock, int reservedStock, int soldStock) {
    }
}
