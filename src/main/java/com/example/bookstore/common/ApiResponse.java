package com.example.bookstore.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 공통 응답 스키마:
 * { isSuccess, message, payload?, meta?, error? }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean isSuccess,
        String message,
        T payload,
        ApiMeta meta,
        ApiError error
) {
    public static <T> ApiResponse<T> ok(String message, T payload) {
        return new ApiResponse<>(true, message, payload, null, null);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null, null, null);
    }

    public static <T> ApiResponse<T> ok(String message, T payload, ApiMeta meta) {
        return new ApiResponse<>(true, message, payload, meta, null);
    }


// ---- alias for assignment spec / legacy controllers ----
public static ApiResponse<Void> success(String message) {
    return ok(message);
}

public static <T> ApiResponse<T> success(String message, T payload) {
    return ok(message, payload);
}

public static <T> ApiResponse<T> success(String message, T payload, ApiMeta meta) {
    return ok(message, payload, meta);
}


    public static ApiResponse<Void> fail(ErrorCode code, String message) {
        return new ApiResponse<>(false, message, null, null, new ApiError(code.name(), null));
    }

    public static ApiResponse<Void> fail(ErrorCode code, String message, Object details) {
        return new ApiResponse<>(false, message, null, null, new ApiError(code.name(), details));
    }
}
