package com.example.bookstore.book.dto;

import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * 도서 수정(관리자/판매자 공통): 전송된 필드만 반영
 * - author_ids/category_ids는 "전량 교체"로 처리
 */
public record BookPatchRequest(
        String title,

        @Min(value = 0, message = "price_cents는 0 이상이어야 합니다.")
        Integer price_cents,

        @Min(value = 0, message = "stock은 0 이상이어야 합니다.")
        Integer stock,

        List<Long> author_ids,

        List<Long> category_ids
) {}
