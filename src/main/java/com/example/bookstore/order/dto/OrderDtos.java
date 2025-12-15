package com.example.bookstore.order.dto;

import com.example.bookstore.order.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public class OrderDtos {

    public record CreateItem(@NotNull Long book_id, @NotNull @Min(1) Integer quantity) {}
    public record CreateOrderRequest(@NotEmpty List<CreateItem> items) {}

    public record CreateOrderResponse(Long order_id, Instant created_at) {}

    public record OrderSummaryDto(Long order_id, OrderStatus status, Integer total_amount_cents, Instant created_at) {}

    public record OrderDetailItemDto(
            Long book_id,
            Long seller_id,
            Integer quantity,
            Integer unit_price_cents,
            Integer subtotal_cents
    ) {}

    public record OrderDetailDto(
            Long order_id,
            OrderStatus status,
            Integer total_amount_cents,
            Instant created_at,
            List<OrderDetailItemDto> items
    ) {}

    public record AdminPatchStatusRequest(@NotNull OrderStatus status) {}

    public record AdminPatchStatusResponse(
            Long order_id,
            OrderStatus previous_status,
            OrderStatus status,
            Instant updated_at
    ) {}
}
