package com.example.bookstore.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handle(ApiException e, HttpServletRequest req) {
        int status = e.code().status().value();
        // 비즈니스 예외는 스택트레이스 대신 요약만
        log.warn("api_exception path={} status={} code={} message={}",
                req.getRequestURI(), status, e.code().name(), e.getMessage());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        req.getRequestURI(),
                        status,
                        e.code().name(),
                        e.getMessage(),
                        e.details()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        int status = ErrorCode.VALIDATION_FAILED.status().value();
        log.warn("validation_failed path={} status={} details={}", req.getRequestURI(), status, details);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        req.getRequestURI(),
                        status,
                        ErrorCode.VALIDATION_FAILED.name(),
                        "입력값이 올바르지 않습니다.",
                        details
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException e, HttpServletRequest req) {
        Map<String, String> details = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : e.getConstraintViolations()) {
            String key = (v.getPropertyPath() == null) ? "param" : v.getPropertyPath().toString();
            details.put(key, v.getMessage());
        }
        int status = ErrorCode.VALIDATION_FAILED.status().value();
        log.warn("constraint_violation path={} status={} details={}", req.getRequestURI(), status, details);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        req.getRequestURI(),
                        status,
                        ErrorCode.VALIDATION_FAILED.name(),
                        "입력값이 올바르지 않습니다.",
                        details.isEmpty() ? null : details
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e, HttpServletRequest req) {
        int status = ErrorCode.INTERNAL_ERROR.status().value();
        // 과제 요구사항: 에러 발생 시 스택트레이스 로그 남기기(민감정보 제외)
        log.error("unhandled_exception path={} status={}", req.getRequestURI(), status, e);

        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        Instant.now().toString(),
                        req.getRequestURI(),
                        status,
                        ErrorCode.INTERNAL_ERROR.name(),
                        "서버 오류가 발생했습니다.",
                        null
                ));
    }
}
