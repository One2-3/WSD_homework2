package com.example.bookstore.book.dto;

public record BookSummaryDto(
        Long id,
        String title,
        Integer price_cents
) {}
