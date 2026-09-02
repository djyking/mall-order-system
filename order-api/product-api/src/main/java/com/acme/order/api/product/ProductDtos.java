package com.acme.order.api.product;

public final class ProductDtos {
    private ProductDtos(){}
    public record SkuView(long skuId,long spuId,String spuName,String skuName,long priceCent,boolean onShelf){}
}
