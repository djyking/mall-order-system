package com.acme.order.api.inventory;

import java.util.List;

/** 库存服务接口使用的数据传输对象集合。 */
public final class InventoryDtos {

    private InventoryDtos() {
    }

    /** 待处理的 SKU 及其数量。 */
    public record Line(long skuId, int quantity) {
    }

    /** 库存预占请求。 */
    public record ReserveRequest(String orderNo, long userId, List<Line> items) {
    }

    /** 库存状态变更请求。 */
    public record ChangeRequest(String orderNo) {
    }

    /** SKU 库存视图。 */
    public record StockView(long skuId, int totalStock, int availableStock, int reservedStock, int soldStock) {
    }
}
