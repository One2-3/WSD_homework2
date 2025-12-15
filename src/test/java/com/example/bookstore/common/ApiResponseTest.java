package com.example.bookstore.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void okFactory_setsSuccessTrue() {
        ApiResponse<Void> res = ApiResponse.ok("OK");
        assertTrue(res.isSuccess());
        assertEquals("OK", res.message());
        assertNull(res.payload());
        assertNull(res.meta());
        assertNull(res.error());
    }

    @Test
    void okFactory_withPayload_setsPayload() {
        ApiResponse<String> res = ApiResponse.ok("OK", "hello");
        assertTrue(res.isSuccess());
        assertEquals("hello", res.payload());
    }

    @Test
    void failFactory_setsError() {
        ApiResponse<Void> res = ApiResponse.fail(ErrorCode.UNAUTHORIZED, "no");
        assertFalse(res.isSuccess());
        assertEquals("no", res.message());
        assertNotNull(res.error());
        assertEquals(ErrorCode.UNAUTHORIZED.name(), res.error().code());
    }

    @Test
    void failFactory_canIncludeDetails() {
        ApiResponse<Void> res = ApiResponse.fail(ErrorCode.VALIDATION_FAILED, "bad", java.util.Map.of("a", "b"));
        assertNotNull(res.error());
        assertEquals(java.util.Map.of("a", "b"), res.error().details());
    }

    @Test
    void aliasSuccess_methodsMatchOk() {
        ApiResponse<String> res = ApiResponse.success("OK", "x");
        assertTrue(res.isSuccess());
        assertEquals("x", res.payload());
    }
}
