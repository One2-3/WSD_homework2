package com.example.bookstore.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handle(ApiException e) {
        return ResponseEntity.status(e.code().status())
                .body(ApiResponse.fail(e.code(), e.getMessage(), e.details()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.status())
                .body(ApiResponse.fail(ErrorCode.VALIDATION_FAILED, "입력값이 올바르지 않습니다.", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.status())
                .body(ApiResponse.fail(ErrorCode.VALIDATION_FAILED, "입력값이 올바르지 않습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e, HttpServletRequest req) {
        // 실제 서비스면 로그 남기기
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR, "서버 오류가 발생했습니다."));
    }
}
