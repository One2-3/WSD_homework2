package com.example.bookstore.sellers.dto;

import com.example.bookstore.sellers.SellerStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SellerPatchRequest(
        String name,
        String contact_email,
        String phone,
        String address,
        String business_no,
        String bank_name,
        String bank_account,
        @Min(0) @Max(10000) Integer commission_bps,
        SellerStatus status
) {}
