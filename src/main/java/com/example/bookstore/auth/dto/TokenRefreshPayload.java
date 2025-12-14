package com.example.bookstore.auth.dto;

public record TokenRefreshPayload(
        String accessToken,
        String refreshToken
) {
}
