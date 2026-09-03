package com.acme.order.product;

import com.acme.order.api.product.ProductDtos.SkuView;
import com.acme.order.common.core.ApiResponse;
import com.acme.order.common.core.BizException;
import com.acme.order.common.core.ErrorCode;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供商品与 SKU 查询接口。
 *
 * @author heyu
 * @since 2026-07-24
 */
@RestController
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping({ "/api/skus/{id}", "/internal/skus/{id}" })
    ApiResponse<SkuView> sku(@PathVariable long id) {
        var sku = repo.sku(id);
        if (!sku.onShelf()) {
            throw new BizException(ErrorCode.SKU_OFF_SHELF, "SKU已下架");
        }
        return ApiResponse.ok(sku, MDC.get("traceId"));
    }
}
