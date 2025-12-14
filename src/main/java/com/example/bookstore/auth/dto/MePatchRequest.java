package com.example.bookstore.auth.dto;

import com.example.bookstore.user.Gender;

import java.time.LocalDate;

/**
 * 내 프로필 수정 요청.
 * - email 또는 newPassword 변경 시 currentPassword 필수
 */
public record MePatchRequest(
        String email,
        String name,
        String address,
        String phone,
        String region,
        Gender gender,
        LocalDate birthdate,
        String currentPassword,
        String newPassword
) {
}
