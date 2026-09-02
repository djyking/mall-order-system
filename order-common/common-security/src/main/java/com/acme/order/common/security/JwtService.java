package com.acme.order.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;
import javax.crypto.SecretKey;

/** JWT 签发与解析服务。 */
public final class JwtService {
  private final SecretKey key;
  private final Duration ttl;

  public JwtService(String secret, Duration ttl) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.ttl = ttl;
  }

  public String issue(long userId, String role) {
    var now = Instant.now();
    return Jwts.builder()
        .subject(Long.toString(userId))
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(ttl)))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
