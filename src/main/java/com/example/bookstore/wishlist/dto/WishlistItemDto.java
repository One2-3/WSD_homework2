package com.example.bookstore.wishlist.dto;

import java.time.Instant;
public record WishlistItemDto(Long book_id, Instant created_at) {}
