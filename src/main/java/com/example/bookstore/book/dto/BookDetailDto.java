package com.example.bookstore.book.dto;

import java.time.Instant;
import java.util.List;

public record BookDetailDto(
        Long id,
        Long seller_id,
        String title,
        Integer price_cents,
        Integer stock,
        Double average_rating,
        Integer ratings_count,
        List<NamedIdDto> authors,
        List<NamedIdDto> categories,
        Instant updated_at
) {}
