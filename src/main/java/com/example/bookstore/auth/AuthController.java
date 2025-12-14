package com.example.bookstore.auth;

import com.example.bookstore.auth.dto.*;
import com.example.bookstore.auth.service.AuthService;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.security.UserPrincipal;
import com.example.bookstore.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    // 1-1. 회원가입: user + access_token + refresh_token fileciteturn13file3L6-L40
    @PostMapping("/register")
    public ApiResponse<AuthPayload> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("회원가입 완료", authService.register(req));
    }

    // 1-2. 로그인: user + access_token + refresh_token
    @PostMapping("/login")
    public ApiResponse<AuthPayload> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("로그인 완료", authService.login(req));
    }

    // 1-3. 로그아웃: refresh_token 폐기 fileciteturn13file2L43-L75
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest req) {
        authService.logout(req);
        return ApiResponse.ok("로그아웃 완료");
    }

    // 1-4. 토큰 갱신: refresh 회전 후 새 access/refresh fileciteturn13file4L34-L73
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshPayload> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok("토큰 갱신", authService.refresh(req));
    }

    // 1-5. 내 프로필 조회 fileciteturn13file0L35-L70
    @GetMapping("/me")
    public ApiResponse<UserPayload> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok("OK", userService.me(principal.userId()));
    }

    // 1-6. 내 프로필 수정
    @PatchMapping("/me")
    public ApiResponse<ProfileUpdatePayload> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody MePatchRequest req
    ) {
        return ApiResponse.ok("프로필이 수정되었습니다.", userService.updateMe(principal.userId(), req));
    }

    // 1-7. 회원 탈퇴(소프트삭제) - 204 (바디 없음) fileciteturn11file2L49-L60
    @PatchMapping("/me/delete")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteMe(principal.userId());
        return ResponseEntity.noContent().build();
    }
}
