package com.example.bookstore.auth;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.security.JwtProperties;
import com.example.bookstore.security.JwtProvider;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRepository;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

/**
 * ⚠️ 레거시/미사용 코드.
 * 실제로는 com.example.bookstore.auth.service.TokenService 를 사용한다.
 * 이 클래스에 @Service가 붙으면 Bean name(tokenService) 충돌이 나므로 제거했다.
 */
public class TokenService {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public TokenService(
            JwtProvider jwtProvider,
            JwtProperties jwtProperties,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository
    ) {
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public record IssuedTokens(String accessToken, String refreshToken) {}

    /** Access JWT + Refresh(plain) 발급, Refresh는 DB에 hash로 저장 */
    @Transactional
    public IssuedTokens issueFor(User user) {
        String access = jwtProvider.createAccessToken(user.getId(), user.getRole());

        String refreshPlain = generateRefreshPlain();
        String refreshHash = hashRefresh(refreshPlain);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(refreshHash);
        rt.setExpiresAt(Instant.now().plus(jwtProperties.refreshExpDays(), ChronoUnit.DAYS));
        rt.setRevokedAt(null);
        refreshTokenRepository.save(rt);

        return new IssuedTokens(access, refreshPlain);
    }

    /** refreshToken(plain) -> DB lookup용 hash */
    public String hashRefresh(String refreshPlain) {
        try {
            String material = refreshPlain + ":" + jwtProperties.refreshPepper();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(material.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "토큰 처리 오류");
        }
    }

    private String generateRefreshPlain() {
        // 길이/엔트로피 충분 (UUID 2개 조합)
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    /** refresh record 검증 + user 로드 */
    public record RefreshContext(RefreshToken token, User user) {}

    public RefreshContext validateRefreshOrThrow(String refreshPlain) {
        String hash = hashRefresh(refreshPlain);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID, "유효하지 않은 refresh token"));

        if (rt.isRevoked()) {
            throw new ApiException(ErrorCode.TOKEN_REVOKED, "폐기된 refresh token");
        }
        if (rt.isExpired(Instant.now())) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, "만료된 refresh token");
        }

        Long userId = rt.getUser().getId();
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        return new RefreshContext(rt, user);
    }
}
