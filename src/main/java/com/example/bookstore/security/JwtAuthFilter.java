package com.example.bookstore.security;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Authorization: Bearer <accessToken> 인증 필터.
 * - 실패 시 과제 요구사항의 ErrorResponse 포맷으로 통일하여 반환한다.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                UserPrincipal principal = jwtProvider.validateAndParseAccessToken(token);

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name().toUpperCase()));
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ApiException e) {
                // TOKEN_EXPIRED / TOKEN_INVALID 등 세부 코드를 유지
                SecurityContextHolder.clearContext();
                writeError(response, request, e.code(), e.getMessage(), e.details());
                return;
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                writeError(response, request, ErrorCode.UNAUTHORIZED, "인증이 필요합니다.", null);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response,
                            HttpServletRequest request,
                            ErrorCode code,
                            String message,
                            Object details) throws IOException {

        int status = code.status().value();
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(
                Instant.now().toString(),
                request.getRequestURI(),
                status,
                code.name(),
                message,
                details
        ));
    }
}
