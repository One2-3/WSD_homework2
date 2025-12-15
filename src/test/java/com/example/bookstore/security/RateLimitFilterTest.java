package com.example.bookstore.security;

import com.example.bookstore.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    @Test
    void shouldNotFilter_skipsSwaggerUi() {
        RateLimitFilter filter = new RateLimitFilter(new ObjectMapper(), 1, 60_000);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(req));
    }

    @Test
    void shouldNotFilter_doesNotSkipApi() {
        RateLimitFilter filter = new RateLimitFilter(new ObjectMapper(), 1, 60_000);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/books");
        assertFalse(filter.shouldNotFilter(req));
    }

    @Test
    void authorizationHeader_skipsRateLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new ObjectMapper(), 1, 60_000);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/books");
        req.addHeader("Authorization", "Bearer x");
        MockHttpServletResponse res = new MockHttpServletResponse();

        final boolean[] called = {false};
        FilterChain chain = (r, s) -> called[0] = true;

        filter.doFilter(req, res, chain);
        assertTrue(called[0]);
        assertEquals(200, res.getStatus());
    }

    @Test
    void blocksAfterLimitPerWindow() throws Exception {
        ObjectMapper om = new ObjectMapper();
        RateLimitFilter filter = new RateLimitFilter(om, 2, 60_000);

        FilterChain chain = (r, s) -> {};

        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/books");
        req1.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, chain);
        assertEquals(200, res1.getStatus());

        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/books");
        req2.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, chain);
        assertEquals(200, res2.getStatus());

        MockHttpServletRequest req3 = new MockHttpServletRequest("GET", "/api/books");
        req3.setRemoteAddr("1.2.3.4");
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        filter.doFilter(req3, res3, chain);
        assertEquals(429, res3.getStatus());

        ErrorResponse parsed = om.readValue(res3.getContentAsByteArray(), ErrorResponse.class);
        assertEquals("TOO_MANY_REQUESTS", parsed.code());
    }

    @Test
    void xForwardedForIsUsedAsClientKey() throws Exception {
        ObjectMapper om = new ObjectMapper();
        RateLimitFilter filter = new RateLimitFilter(om, 1, 60_000);
        FilterChain chain = (r, s) -> {};

        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/books");
        req1.addHeader("X-Forwarded-For", "9.9.9.9, 1.1.1.1");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilter(req1, res1, chain);
        assertEquals(200, res1.getStatus());

        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/books");
        req2.addHeader("X-Forwarded-For", "9.9.9.9");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilter(req2, res2, chain);
        assertEquals(429, res2.getStatus());
    }
}
