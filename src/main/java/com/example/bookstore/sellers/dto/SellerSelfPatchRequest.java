package com.example.bookstore.sellers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 판매자 본인 정보 수정 요청(SELLER 전용)
 * - status/commission 등 운영 필드는 수정 불가(ADMIN 전용)
 * - bank_account는 입력 전용이며 저장 시 마스킹하여 bank_account_masked로 저장
 */
public record SellerSelfPatchRequest(
        @Email String contact_email,
        String phone,
        String address,
        String bank_name,
        @Size(min = 4, max = 64) String bank_account
) {}
