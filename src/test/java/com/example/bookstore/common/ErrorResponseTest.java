package com.example.bookstore.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void canCreateErrorResponse() {
        ErrorResponse er = new ErrorResponse("2025-01-01T00:00:00Z", "/x", 400, "BAD_REQUEST", "bad", null);
        assertEquals("/x", er.path());
        assertEquals(400, er.status());
        assertEquals("BAD_REQUEST", er.code());
    }

    @Test
    void detailsCanBeNull() {
        ErrorResponse er = new ErrorResponse("t", "/", 500, "INTERNAL", "oops", null);
        assertNull(er.details());
    }

    @Test
    void detailsCanBeObject() {
        var details = java.util.Map.of("field", "reason");
        ErrorResponse er = new ErrorResponse("t", "/", 422, "VALIDATION_FAILED", "invalid", details);
        assertEquals(details, er.details());
    }

    @Test
    void messageIsSeparateFromCode() {
        ErrorResponse er = new ErrorResponse("t", "/", 401, "UNAUTHORIZED", "need auth", null);
        assertEquals("UNAUTHORIZED", er.code());
        assertEquals("need auth", er.message());
    }
}
