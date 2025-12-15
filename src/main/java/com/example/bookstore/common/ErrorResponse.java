package com.example.bookstore.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 과제 요구사항의 표준 에러 응답 포맷.
 */
@Schema(
        name = "ErrorResponse",
        description = "과제 요구사항의 표준 에러 응답 포맷"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "에러 발생 시각(ISO 8601)", example = "2025-03-05T12:34:56Z")
        String timestamp,

        @Schema(description = "요청 경로", example = "/api/posts/1")
        String path,

        @Schema(description = "HTTP 상태 코드", example = "422")
        int status,

        @Schema(description = "시스템 내부 에러 코드", example = "VALIDATION_FAILED")
        String code,

        @Schema(description = "사용자에게 전달할 메시지", example = "입력값이 올바르지 않습니다.")
        String message,

        @Schema(description = "(선택) 필드별 오류/상세 사유")
        Object details
) {
}
