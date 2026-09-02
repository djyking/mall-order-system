package com.acme.order.common.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 基于 Redis 的订单幂等令牌服务。 */
public final class IdempotencyTokenService {
  private final StringRedisTemplate redis;

  public IdempotencyTokenService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public boolean acquire(long userId, String token) {
    return Boolean.TRUE.equals(
        redis
            .opsForValue()
            .setIfAbsent(
                "order:idempotency:create:" + userId + ":" + token,
                "PROCESSING",
                Duration.ofMinutes(10)));
  }

  public void complete(long userId, String token, String orderNo) {
    redis
        .opsForValue()
        .set("order:idempotency:create:" + userId + ":" + token, orderNo, Duration.ofMinutes(10));
  }

  public void release(long userId, String token) {
    redis.delete("order:idempotency:create:" + userId + ":" + token);
  }
}
