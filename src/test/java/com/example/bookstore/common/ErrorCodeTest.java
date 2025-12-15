package com.example.bookstore.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {

    @Test
    void hasAtLeastTenCodes_forAssignment() {
        assertTrue(ErrorCode.values().length >= 10);
    }

    @Test
    void eachCodeHasStatus() {
        for (ErrorCode c : ErrorCode.values()) {
            assertNotNull(c.status());
        }
    }

    @Test
    void unauthorizedFamily_forTokenErrors() {
        assertEquals(401, ErrorCode.TOKEN_INVALID.status().value());
        assertEquals(401, ErrorCode.TOKEN_EXPIRED.status().value());
        assertEquals(401, ErrorCode.TOKEN_REVOKED.status().value());
    }

    @Test
    void badRequestCodes_are400() {
        assertEquals(400, ErrorCode.BAD_REQUEST.status().value());
        assertEquals(400, ErrorCode.INVALID_QUERY_PARAM.status().value());
    }

    @Test
    void tooManyRequests_is429() {
        assertEquals(429, ErrorCode.TOO_MANY_REQUESTS.status().value());
    }
}
