package com.acme.order.inventory;

import jakarta.validation.Valid;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.order.api.inventory.InventoryDtos.ChangeRequest;
import com.acme.order.api.inventory.InventoryDtos.ReserveRequest;
import com.acme.order.api.inventory.InventoryDtos.StockView;
import com.acme.order.common.core.ApiResponse;

/** 提供库存预占、确认、释放与查询接口。 */
@RestController
@RequestMapping("/internal/inventory")
public class InventoryController {

    private final InventoryService service;
    private final InventoryRepository repo;

    public InventoryController(InventoryService s, InventoryRepository r) {
        service = s;
        repo = r;
    }

    @PostMapping("/reserve")
    ApiResponse<Void> reserve(@Valid @RequestBody ReserveRequest r) {
        service.reserve(r);
        return ApiResponse.ok(null, MDC.get("traceId"));
    }

    @PostMapping("/confirm")
    ApiResponse<Integer> confirm(@RequestBody ChangeRequest r) {
        return ApiResponse.ok(service.confirm(r.orderNo()), MDC.get("traceId"));
    }

    @PostMapping("/release")
    ApiResponse<Integer> release(@RequestBody ChangeRequest r) {
        return ApiResponse.ok(service.release(r.orderNo()), MDC.get("traceId"));
    }

    @GetMapping("/{sku}")
    ApiResponse<StockView> stock(@PathVariable long sku) {
        return ApiResponse.ok(repo.stock(sku), MDC.get("traceId"));
    }
}
