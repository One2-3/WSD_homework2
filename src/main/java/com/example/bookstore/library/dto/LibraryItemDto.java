package com.example.bookstore.library.dto;

import java.time.Instant;

public record LibraryItemDto(Long book_id, Instant acquired_at) {}
