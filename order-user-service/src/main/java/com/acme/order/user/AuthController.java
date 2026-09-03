package com.acme.order.user;

import com.acme.order.api.user.UserDtos.LoginResponse;
import com.acme.order.api.user.UserDtos.UserView;
import com.acme.order.common.core.ApiResponse;
import com.acme.order.common.core.BizException;
import com.acme.order.common.core.ErrorCode;
import com.acme.order.common.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供用户认证及当前用户查询接口。
 *
 * @author heyu
 * @since 2026-07-24
 */
@RestController
public class AuthController {

    private final JwtService jwt;

    public AuthController(JwtService jwt) {
        this.jwt = jwt;
    }

    @PostMapping("/api/auth/login")
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginBody body) {
        if (!"demo".equals(body.username()) || !"demo123".equals(body.password())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return ApiResponse.ok(new LoginResponse(10001, "demo", "USER", jwt.issue(10001, "USER")), MDC.get("traceId"));
    }

    @GetMapping("/api/users/me")
    ApiResponse<UserView> me(@RequestHeader(value = "X-User-Id", defaultValue = "10001") long userId) {
        return ApiResponse.ok(new UserView(userId, "demo", "USER"), MDC.get("traceId"));
    }

    /**
     * 登录请求参数。
     *
     * @param username 用户名
     * @param password 密码
     * @author heyu
     * @since 2026-07-24
     */
    public record LoginBody(@NotBlank String username, @NotBlank String password) {
    }
}
