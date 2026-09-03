package com.acme.order.order;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.acme.order.common.core.ApiResponse;
import org.slf4j.MDC;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供秒杀库存预占与结果查询接口。
 *
 * @author heyu
 * @since 2026-08-12
 */
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> script;

    public SeckillController(StringRedisTemplate r) {
        redis = r;
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill-reserve.lua"));
        script.setResultType(Long.class);
    }

    @PostMapping("/{sku}")
    ApiResponse<Map<String, String>> reserve(@PathVariable long sku, @RequestParam(defaultValue = "1") int quantity,
        @RequestHeader("X-User-Id") long user) {
        String request = UUID.randomUUID().toString();
        Boolean first = redis.opsForValue().setIfAbsent("order:seckill:user:" + sku + ":" + user, request,
            java.time.Duration.ofMinutes(10));
        String status;
        if (!Boolean.TRUE.equals(first)) {
            status = "DUPLICATE";
        } else {
            Long ok = redis.execute(script, List.of("order:stock:available:" + sku), Integer.toString(quantity));
            status = ok != null && ok == 1 ? "QUEUING" : "SOLD_OUT";
            if (!"QUEUING".equals(status)) {
                redis.delete("order:seckill:user:" + sku + ":" + user);
            }
        }
        redis.opsForValue().set("order:seckill:result:" + request, status, java.time.Duration.ofHours(1));
        return ApiResponse.ok(Map.of("requestId", request, "status", status), MDC.get("traceId"));
    }

    @GetMapping("/result/{id}")
    ApiResponse<Map<String, String>> result(@PathVariable String id) {
        String status = redis.opsForValue().get("order:seckill:result:" + id);
        return ApiResponse.ok(Map.of("requestId", id, "status", status == null ? "NOT_FOUND" : status),
            MDC.get("traceId"));
    }
}
