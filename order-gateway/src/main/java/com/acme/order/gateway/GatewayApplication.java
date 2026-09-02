package com.acme.order.gateway;
import com.acme.order.common.security.JwtService;import org.springframework.beans.factory.annotation.Value;import org.springframework.boot.*;import org.springframework.boot.autoconfigure.*;import org.springframework.context.annotation.*;import java.time.Duration;
@SpringBootApplication public class GatewayApplication {public static void main(String[] args){SpringApplication.run(GatewayApplication.class,args);}@Bean JwtService jwtService(@Value("${security.jwt.secret}")String secret){return new JwtService(secret,Duration.ofHours(8));}}
