package com.acme.order.api.product;

/** 商品服务接口使用的数据传输对象集合。 */
public final class ProductDtos {
  private ProductDtos() {}

  /** SKU 商品视图。 */
  public record SkuView(
      long skuId, long spuId, String spuName, String skuName, long priceCent, boolean onShelf) {}
}
