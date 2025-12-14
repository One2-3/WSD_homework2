package com.example.bookstore.auth.service;

import com.example.bookstore.auth.dto.*;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRepository;
import com.example.bookstore.user.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthPayload register(RegisterRequest req) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new ApiException(ErrorCode.CONFLICT, "요청이 현재 리소스 상태와 충돌합니다.");
        }

        User u = new User();
        u.setEmail(req.email());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setName(req.name());
        u.setAddress(req.address());
        u.setPhone(req.phone());
        u.setRegion(req.region());

        userRepository.save(u);

        TokenService.TokenPair pair = tokenService.issue(u);
        return new AuthPayload(UserDto.from(u), pair.accessToken(), pair.refreshToken());
    }

    @Transactional(readOnly = true)
    public User requireActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Transactional
    public AuthPayload login(LoginRequest req) {
        User u = requireActiveUserByEmail(req.email());
        if (!passwordEncoder.matches(req.password(), u.getPassword())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        TokenService.TokenPair pair = tokenService.issue(u);
        return new AuthPayload(UserDto.from(u), pair.accessToken(), pair.refreshToken());
    }

    @Transactional
    public TokenRefreshPayload refresh(RefreshRequest req) {
        TokenService.TokenPair pair = tokenService.rotate(req.refreshToken());
        return new TokenRefreshPayload(pair.accessToken(), pair.refreshToken());
    }

    @Transactional
    public void logout(LogoutRequest req) {
        tokenService.revoke(req.refreshToken());
    }
}
