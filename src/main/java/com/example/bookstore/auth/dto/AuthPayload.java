package com.example.bookstore.auth.dto;

import com.example.bookstore.user.dto.UserDto;

public record AuthPayload(
        UserDto user,
        String accessToken,
        String refreshToken
) {
}
