package com.example.bookstore.sellers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ADMIN이 판매자(seller) 로그인 계정을 생성할 때 사용하는 DTO.
 * - name은 null이면 seller.name을 기본으로 사용
 */
public record SellerAccountCreateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        String name
) {}
