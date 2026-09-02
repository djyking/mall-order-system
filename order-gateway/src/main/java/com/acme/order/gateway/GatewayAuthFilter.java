package com.acme.order.gateway;

import com.acme.order.common.security.JwtService;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** 校验访问令牌并向下游透传用户身份的网关过滤器。 */
@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {
  private final JwtService jwt;
  private final boolean dev;

  public GatewayAuthFilter(JwtService j, @Value("${security.allow-dev-header:false}") boolean d) {
    jwt = j;
    dev = d;
  }

  public Mono<Void> filter(
      org.springframework.web.server.ServerWebExchange e, GatewayFilterChain c) {
    String path = e.getRequest().getPath().value();
    if (path.startsWith("/api/auth/")
        || path.startsWith("/api/products/")
        || path.startsWith("/api/skus/")
        || path.startsWith("/actuator/")) return c.filter(e);
    if (dev && e.getRequest().getHeaders().containsKey("X-User-Id")) return c.filter(e);
    String auth = e.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    try {
      if (auth == null || !auth.startsWith("Bearer ")) throw new IllegalArgumentException();
      var claims = jwt.parse(auth.substring(7));
      var req =
          e.getRequest()
              .mutate()
              .headers(
                  h -> {
                    h.set("X-User-Id", claims.getSubject());
                    h.set("X-User-Role", claims.get("role", String.class));
                  })
              .build();
      return c.filter(e.mutate().request(req).build());
    } catch (Exception ex) {
      e.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      e.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
      var data =
          e.getResponse()
              .bufferFactory()
              .wrap(
                  "{\"code\":\"UNAUTHORIZED\",\"message\":\"invalid or missing token\",\"data\":null}"
                      .getBytes(StandardCharsets.UTF_8));
      return e.getResponse().writeWith(Mono.just(data));
    }
  }

  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 10;
  }
}
