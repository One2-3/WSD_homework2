
package com.example.bookstore.stats.dto;

import java.time.LocalDate;
import java.util.List;

public class StatsDtos {

    public record DailySalesDto(LocalDate date, Integer gross_cents, Integer orders_count) {}

    public record TopBookDto(Long book_id, String title, Integer sold_qty, Integer gross_cents) {}

    public record TopSellerDto(Long seller_id, String name, Integer gross_cents) {}

    public record DailySalesPayload(List<DailySalesDto> items) {}

    public record TopBooksPayload(List<TopBookDto> items) {}

    public record TopSellersPayload(List<TopSellerDto> items) {}
}
