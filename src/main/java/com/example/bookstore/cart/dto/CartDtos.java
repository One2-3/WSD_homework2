package com.example.bookstore.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CartDtos {

    public record AddItemRequest(@NotNull Long book_id, @NotNull @Min(1) Integer quantity) {}
    public record PatchItemRequest(@NotNull @Min(1) Integer quantity) {}

    public record CartItemView(
            Long item_id,
            Long book_id,
            Integer quantity,
            Integer unit_price_cents,
            Integer line_total_cents
    ) {}

    public record CartView(
            List<CartItemView> items,
            Integer subtotal_cents,
            Integer total_qty
    ) {}
}
