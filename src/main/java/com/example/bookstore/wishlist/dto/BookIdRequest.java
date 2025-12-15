package com.example.bookstore.wishlist.dto;

import jakarta.validation.constraints.NotNull;
public record BookIdRequest(@NotNull Long book_id) {}
