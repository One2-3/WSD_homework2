package com.example.bookstore.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 도서 등록(관리자) 요청: seller_id/title/price_cents/stock + author_ids(1..N) + category_ids(1..N)
 */
public record BookCreateRequest(
        @NotNull(message = "seller_id는 필수입니다.")
        Long seller_id,

        @NotBlank(message = "title은 필수입니다.")
        String title,

        @NotNull(message = "price_cents는 필수입니다.")
        @Min(value = 0, message = "price_cents는 0 이상이어야 합니다.")
        Integer price_cents,

        @NotNull(message = "stock은 필수입니다.")
        @Min(value = 0, message = "stock은 0 이상이어야 합니다.")
        Integer stock,

        @NotNull(message = "author_ids는 필수입니다.")
        @Size(min = 1, message = "author_ids는 1개 이상이어야 합니다.")
        List<Long> author_ids,

        @NotNull(message = "category_ids는 필수입니다.")
        @Size(min = 1, message = "category_ids는 1개 이상이어야 합니다.")
        List<Long> category_ids
) {}
