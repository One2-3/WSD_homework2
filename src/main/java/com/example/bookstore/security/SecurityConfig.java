package com.example.bookstore.security;

import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.common.ErrorResponse;
import com.example.bookstore.common.RequestLoggingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public JwtProvider jwtProvider(JwtProperties props) {
        return new JwtProvider(props);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        return new JwtAuthFilter(jwtProvider, objectMapper);
    }

    @Bean
    public RateLimitFilter rateLimitFilter(ObjectMapper objectMapper) {
        // IP 당 60 req / 1분 (인증 없는 요청에만 적용)
        return new RateLimitFilter(objectMapper, 60, 60_000L);
    }

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RequestLoggingFilter requestLoggingFilter,
                                                   RateLimitFilter rateLimitFilter,
                                                   JwtAuthFilter jwtAuthFilter,
                                                   ObjectMapper om)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            om.writeValue(res.getWriter(), new ErrorResponse(
                                    java.time.Instant.now().toString(),
                                    req.getRequestURI(),
                                    401,
                                    ErrorCode.UNAUTHORIZED.name(),
                                    "인증이 필요합니다.",
                                    null
                            ));
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            om.writeValue(res.getWriter(), new ErrorResponse(
                                    java.time.Instant.now().toString(),
                                    req.getRequestURI(),
                                    403,
                                    ErrorCode.FORBIDDEN.name(),
                                    "권한이 없습니다.",
                                    null
                            ));
                        })
                );

        // request -> logging -> rate limit -> jwt auth -> controller
        http.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
