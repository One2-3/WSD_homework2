package com.example.bookstore.auth.service;

import com.example.bookstore.auth.RefreshToken;
import com.example.bookstore.auth.RefreshTokenRepository;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.security.JwtProperties;
import com.example.bookstore.security.JwtProvider;
import com.example.bookstore.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class TokenService {

    private final RefreshTokenRepository tokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public record TokenPair(String accessToken, String refreshToken) {}

    public TokenService(RefreshTokenRepository tokenRepository, JwtProvider jwtProvider, JwtProperties jwtProperties) {
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public TokenPair issue(User user) {
        String access = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String refreshRaw = generateRefreshToken();

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash(refreshRaw));
        rt.setExpiresAt(Instant.now().plus(jwtProperties.refreshExpDays(), ChronoUnit.DAYS));
        tokenRepository.save(rt);

        return new TokenPair(access, refreshRaw);
    }

    @Transactional
    public TokenPair rotate(String refreshRaw) {
        String hash = hash(refreshRaw);
        RefreshToken token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID, "유효하지 않은 토큰입니다."));

        Instant now = Instant.now();
        if (token.isRevoked()) throw new ApiException(ErrorCode.TOKEN_REVOKED, "폐기된 토큰입니다.");
        if (token.isExpired(now)) throw new ApiException(ErrorCode.TOKEN_EXPIRED, "토큰이 만료되었습니다.");

        User user = token.getUser();
        if (user.isDeleted()) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");

        token.setRevokedAt(now); // 기존 refresh 폐기(회전)
        return issue(user); // 새 refresh 발급 + 새 access 발급
    }

    @Transactional
    public void revoke(String refreshRaw) {
        tokenRepository.revokeByHash(hash(refreshRaw), Instant.now());
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        tokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String refreshRaw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String salted = jwtProperties.refreshPepper() + ":" + refreshRaw;
            byte[] digest = md.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}
