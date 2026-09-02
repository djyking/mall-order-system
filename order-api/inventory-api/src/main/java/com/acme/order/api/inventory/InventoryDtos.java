package com.acme.order.api.inventory;

import java.util.List;
public final class InventoryDtos {
    private InventoryDtos(){}
    public record Line(long skuId,int quantity){}
    public record ReserveRequest(String orderNo,long userId,List<Line> items){}
    public record ChangeRequest(String orderNo){}
    public record StockView(long skuId,int totalStock,int availableStock,int reservedStock,int soldStock){}
}
