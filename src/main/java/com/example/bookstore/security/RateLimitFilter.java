package com.example.bookstore.security;

import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 간단한 인메모리 레이트리밋 필터.
 * - 인증 없는 요청(Authorization 헤더가 없는 요청)에만 적용
 * - 기본: IP 당 60 req / 1분
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper om;
    private final int limitPerWindow;
    private final long windowMs;

    private static final class Window {
        volatile long windowStartMs;
        final AtomicInteger count = new AtomicInteger(0);
        Window(long startMs) { this.windowStartMs = startMs; }
    }

    private final ConcurrentHashMap<String, Window> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper om, int limitPerWindow, long windowMs) {
        this.om = om;
        this.limitPerWindow = limitPerWindow;
        this.windowMs = windowMs;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 문서/정적 리소스는 제외
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/static")
                || path.startsWith("/favicon");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && !auth.isBlank()) {
            // Authorization 헤더가 있으면 인증 요청으로 보고 레이트리밋 제외
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        String key = clientKey(request);

        Window w = counters.compute(key, (k, old) -> {
            if (old == null) {
                Window nw = new Window(now);
                nw.count.incrementAndGet();
                return nw;
            }
            if (now - old.windowStartMs >= windowMs) {
                old.windowStartMs = now;
                old.count.set(0);
            }
            old.count.incrementAndGet();
            return old;
        });

        if (w.count.get() > limitPerWindow) {
            int status = ErrorCode.TOO_MANY_REQUESTS.status().value();
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            om.writeValue(response.getWriter(), new ErrorResponse(
                    Instant.now().toString(),
                    request.getRequestURI(),
                    status,
                    ErrorCode.TOO_MANY_REQUESTS.name(),
                    "요청이 너무 많습니다.",
                    Map.of("limit_per_minute", limitPerWindow)
            ));
            return;
        }

        // 아주 간단한 청소(메모리 누수 방지용)
        if (counters.size() > 10_000) {
            counters.clear();
        }

        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        // 프록시 환경이면 X-Forwarded-For 사용 가능
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
