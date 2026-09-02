package com.acme.order.api.user;

/** 用户服务接口使用的数据传输对象集合。 */
public final class UserDtos {

    private UserDtos() {
    }

    /** 用户登录请求。 */
    public record LoginRequest(String username, String password) {
    }

    /** 用户登录结果。 */
    public record LoginResponse(long userId, String username, String role, String token) {
    }

    /** 用户信息视图。 */
    public record UserView(long userId, String username, String role) {
    }
}
