
package com.example.bookstore.settlement.dto;

import com.example.bookstore.settlement.SettlementStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class SettlementDtos {

    public record CreateSettlementRequest(
            @NotNull LocalDate period_start,
            @NotNull LocalDate period_end
    ) {}

    public record CreateSettlementResponse(
            int created,
            List<Long> settlement_ids
    ) {}
    public record SettlementSummaryDto(
            Long id,
            Long seller_id,
            LocalDate period_start,
            LocalDate period_end,
            SettlementStatus status,
            Integer total_gross_cents,
            Integer total_commission_cents,
            Integer total_net_cents,
            Instant paid_at,
            Instant seller_confirmed_at,
            Instant created_at,
            Instant updated_at
    ) {}

    public record SettlementItemDto(
            Long id,
            Long order_item_id,
            Long seller_id,
            Integer gross_cents,
            Integer commission_cents,
            Integer net_cents,
            Instant created_at
    ) {}

    public record SettlementDetailDto(
            SettlementSummaryDto settlement,
            List<SettlementItemDto> items
    ) {}
}
