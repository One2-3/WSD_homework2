package com.example.bookstore.library.dto;

import jakarta.validation.constraints.NotNull;

public record BookIdRequest(@NotNull Long book_id) {}
