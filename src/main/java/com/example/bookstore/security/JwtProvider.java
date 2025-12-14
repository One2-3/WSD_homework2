package com.example.bookstore.security;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtProvider {

    private final JwtProperties props;
    private final SecretKey accessKey;

    public JwtProvider(JwtProperties props) {
        this.props = props;
        if (props.accessSecret() == null || props.accessSecret().length() < 32) {
            throw new IllegalArgumentException("app.jwt.access-secret must be at least 32 chars");
        }
        this.accessKey = Keys.hmacShaKeyFor(props.accessSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, UserRole role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessExpMinutes() * 60);

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(accessKey)
                .compact();
    }

    public UserPrincipal validateAndParseAccessToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token);

            Claims c = jws.getPayload();
            Long userId = Long.valueOf(c.getSubject());
            String role = c.get("role", String.class);

            return new UserPrincipal(userId, UserRole.valueOf(role));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, "토큰이 만료되었습니다.");
        } catch (Exception e) {
            throw new ApiException(ErrorCode.TOKEN_INVALID, "유효하지 않은 토큰입니다.");
        }
    }
}
