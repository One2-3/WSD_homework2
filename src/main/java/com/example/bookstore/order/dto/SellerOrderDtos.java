package com.example.bookstore.order.dto;

import com.example.bookstore.order.OrderStatus;

import java.time.Instant;
import java.util.List;

public class SellerOrderDtos {

    /**
     * 판매자 주문 아이템 목록(주문 단위가 아니라 아이템 단위)
     */
    public record SellerOrderItemDto(
            Long order_id,
            Long user_id,
            OrderStatus status,
            Instant created_at,
            Long book_id,
            Integer quantity,
            Integer unit_price_cents,
            Integer subtotal_cents
    ) {}

    public record SellerOrderItemLineDto(
            Long book_id,
            Integer quantity,
            Integer unit_price_cents,
            Integer subtotal_cents
    ) {}

    public record SellerOrderDetailDto(
            Long order_id,
            Long user_id,
            OrderStatus status,
            Integer order_total_amount_cents,
            Integer seller_total_amount_cents,
            Instant created_at,
            List<SellerOrderItemLineDto> items
    ) {}
}
