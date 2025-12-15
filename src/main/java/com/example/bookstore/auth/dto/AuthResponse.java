package com.example.bookstore.auth.dto;

import com.example.bookstore.user.dto.UserDto;

public record AuthResponse(
        UserDto user,
        String accessToken,
        String refreshToken
) {}
