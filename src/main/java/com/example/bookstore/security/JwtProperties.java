package com.example.bookstore.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        String accessSecret,
        long accessExpMinutes,
        String refreshPepper,
        long refreshExpDays
) {
}
