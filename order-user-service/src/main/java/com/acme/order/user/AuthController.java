package com.acme.order.user;

import com.acme.order.api.user.UserDtos.*;
import com.acme.order.common.core.*;
import com.acme.order.common.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

/** 提供用户认证及当前用户查询接口。 */
@RestController
public class AuthController {
  private final JwtService jwt;

  public AuthController(JwtService jwt) {
    this.jwt = jwt;
  }

  /** 登录请求参数。 */
  public record LoginBody(@NotBlank String username, @NotBlank String password) {}

  @PostMapping("/api/auth/login")
  ApiResponse<LoginResponse> login(@Valid @RequestBody LoginBody body) {
    if (!"demo".equals(body.username()) || !"demo123".equals(body.password()))
      throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
    return ApiResponse.ok(
        new LoginResponse(10001, "demo", "USER", jwt.issue(10001, "USER")), MDC.get("traceId"));
  }

  @GetMapping("/api/users/me")
  ApiResponse<UserView> me(
      @RequestHeader(value = "X-User-Id", defaultValue = "10001") long userId) {
    return ApiResponse.ok(new UserView(userId, "demo", "USER"), MDC.get("traceId"));
  }
}
