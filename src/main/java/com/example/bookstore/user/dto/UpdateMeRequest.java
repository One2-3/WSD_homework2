package com.example.bookstore.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMeRequest(
        // 기본 프로필
        String name,
        String address,
        String region,
        String phone,

        // 선택 변경
        @Email String email,

        // 비번 변경: newPassword 제공 시 currentPassword 필수
        String currentPassword,
        @Size(min = 8, max = 72) String newPassword
) {}
