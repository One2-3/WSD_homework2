package com.example.bookstore.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 과제 요구사항의 표준 에러 응답 포맷.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String timestamp,
        String path,
        int status,
        String code,
        String message,
        Object details
) {
}
