package com.example.bookstore.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

    TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED(HttpStatus.UNAUTHORIZED);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
