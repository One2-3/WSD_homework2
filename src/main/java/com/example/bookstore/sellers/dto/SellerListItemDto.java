package com.example.bookstore.sellers.dto;

import com.example.bookstore.sellers.Seller;
import com.example.bookstore.sellers.SellerStatus;

public record SellerListItemDto(
        Long id,
        String name,
        SellerStatus status,
        Integer commission_bps
) {
    public static SellerListItemDto from(Seller s) {
        return new SellerListItemDto(s.getId(), s.getName(), s.getStatus(), s.getCommissionBps());
    }
}
