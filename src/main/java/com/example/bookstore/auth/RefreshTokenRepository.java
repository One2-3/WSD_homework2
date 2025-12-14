package com.example.bookstore.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.tokenHash = :hash and t.revokedAt is null")
    int revokeByHash(@Param("hash") String hash, @Param("now") Instant now);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.user.id = :userId and t.revokedAt is null")
    int revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}
