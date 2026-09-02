package com.acme.order.product;

import com.acme.order.api.product.ProductDtos.SkuView;
import com.acme.order.common.core.*;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

/** 提供商品与 SKU 查询接口。 */
@RestController
public class ProductController {
  private final ProductRepository repo;

  public ProductController(ProductRepository repo) {
    this.repo = repo;
  }

  @GetMapping({"/api/skus/{id}", "/internal/skus/{id}"})
  ApiResponse<SkuView> sku(@PathVariable long id) {
    var sku = repo.sku(id);
    if (!sku.onShelf()) throw new BizException(ErrorCode.SKU_OFF_SHELF, "SKU已下架");
    return ApiResponse.ok(sku, MDC.get("traceId"));
  }
}
