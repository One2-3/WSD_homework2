package com.example.bookstore.sellers.dto;

import java.time.Instant;

import com.example.bookstore.sellers.Seller;
import com.example.bookstore.sellers.SellerStatus;

public record SellerDto(
        Long id,
        String name,
        String contact_email,
        String phone,
        String address,
        String business_no_masked,
        String bank_name,
        String bank_account_masked,
        Integer commission_bps,
        SellerStatus status,
        Instant created_at,
        Instant updated_at
) {
    public static SellerDto from(Seller s) {
        return new SellerDto(
                s.getId(),
                s.getName(),
                s.getContactEmail(),
                s.getPhone(),
                s.getAddress(),
                maskBusinessNo(s.getBusinessNo()),
                s.getBankName(),
                s.getBankAccountMasked(),
                s.getCommissionBps(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    private static String maskBusinessNo(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() <= 5) return "*****";
        String tail = digits.substring(digits.length() - 5);
        return "***-**-" + tail;
    }
}
