package com.example.bookstore.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 판매자 도서 등록 요청
 * - author_ids/category_ids는 선택(없으면 연결 테이블에 행을 만들지 않음)
 */
public record SellerBookCreateRequest(
        @NotBlank(message = "title은 필수입니다.")
        String title,

        @NotNull(message = "price_cents는 필수입니다.")
        @Min(value = 0, message = "price_cents는 0 이상이어야 합니다.")
        Integer price_cents,

        @NotNull(message = "stock은 필수입니다.")
        @Min(value = 0, message = "stock은 0 이상이어야 합니다.")
        Integer stock,

        List<Long> author_ids,
        List<Long> category_ids
) {}
