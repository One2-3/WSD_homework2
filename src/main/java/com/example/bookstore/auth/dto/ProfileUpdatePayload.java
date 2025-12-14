package com.example.bookstore.auth.dto;

import com.example.bookstore.user.dto.UserDto;

/**
 * email/newPassword 변경 시 accessToken/refreshToken 포함(없으면 null)
 */
public record ProfileUpdatePayload(
        UserDto user,
        String accessToken,
        String refreshToken
) {
}
