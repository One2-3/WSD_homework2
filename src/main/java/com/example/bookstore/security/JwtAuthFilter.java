package com.example.bookstore.security;

import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ErrorCode;
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
import java.util.List;

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
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(),
                        ApiResponse.fail(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
