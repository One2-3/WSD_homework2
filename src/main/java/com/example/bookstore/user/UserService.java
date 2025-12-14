package com.example.bookstore.user;

import com.example.bookstore.auth.dto.MePatchRequest;
import com.example.bookstore.auth.dto.ProfileUpdatePayload;
import com.example.bookstore.auth.dto.UserPayload;
import com.example.bookstore.auth.service.TokenService;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.user.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public UserPayload me(Long userId) {
        User u = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리소스를 찾을 수 없습니다."));
        return new UserPayload(UserDto.from(u));
    }

    @Transactional
    public ProfileUpdatePayload updateMe(Long userId, MePatchRequest req) {
        User u = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리소스를 찾을 수 없습니다."));

        boolean sensitiveChange = (req.email() != null && !req.email().isBlank() && !req.email().equals(u.getEmail()))
                || (req.newPassword() != null && !req.newPassword().isBlank());

        if (sensitiveChange) {
            if (req.currentPassword() == null || req.currentPassword().isBlank()) {
                throw new ApiException(ErrorCode.VALIDATION_FAILED, "입력값이 올바르지 않습니다.",
                        java.util.Map.of("current_password", "email 또는 new_password 변경 시 필수"));
            }
            if (!passwordEncoder.matches(req.currentPassword(), u.getPassword())) {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");
            }
        }

        if (req.email() != null && !req.email().isBlank() && !req.email().equals(u.getEmail())) {
            if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
                throw new ApiException(ErrorCode.CONFLICT, "요청이 현재 리소스 상태와 충돌합니다.");
            }
            u.setEmail(req.email());
        }

        if (req.name() != null) u.setName(req.name());
        if (req.address() != null) u.setAddress(req.address());
        if (req.phone() != null) u.setPhone(req.phone());
        if (req.region() != null) u.setRegion(req.region());
        if (req.gender() != null) u.setGender(req.gender());
        if (req.birthdate() != null) u.setBirthdate(req.birthdate());

        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.newPassword()));
        }

        // 토큰 회전(요건): email/newPassword 변경 시 refresh 전부 폐기 후 재발급
        if (sensitiveChange) {
            tokenService.revokeAllForUser(userId);
            TokenService.TokenPair pair = tokenService.issue(u);
            return new ProfileUpdatePayload(UserDto.from(u), pair.accessToken(), pair.refreshToken());
        }

        return new ProfileUpdatePayload(UserDto.from(u), null, null);
    }

    @Transactional
    public void deleteMe(Long userId) {
        User u = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리소스를 찾을 수 없습니다."));
        u.setDeletedAt(Instant.now());
        tokenService.revokeAllForUser(userId);
    }

    // Admin
    @Transactional
    public void deactivateUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리소스를 찾을 수 없습니다."));
        u.setDeletedAt(Instant.now());
        tokenService.revokeAllForUser(userId);
    }
}
