package com.acme.order.api.user;

public final class UserDtos {
    private UserDtos(){}
    public record LoginRequest(String username,String password){}
    public record LoginResponse(long userId,String username,String role,String token){}
    public record UserView(long userId,String username,String role){}
}
