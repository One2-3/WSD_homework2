package com.example.bookstore.user;

import com.example.bookstore.auth.dto.MePatchRequest;
import com.example.bookstore.auth.dto.ProfileUpdatePayload;
import com.example.bookstore.auth.dto.RegisterRequest;
import com.example.bookstore.auth.dto.AuthPayload;
import com.example.bookstore.auth.dto.UserPayload;
import com.example.bookstore.auth.service.AuthService;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    // 과제용 alias: POST /api/users (회원가입)
    @PostMapping
    public ApiResponse<AuthPayload> signup(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("회원가입 완료", authService.register(req));
    }

    @GetMapping("/me")
    public ApiResponse<UserPayload> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok("OK", userService.me(principal.userId()));
    }

    @PatchMapping("/me")
    public ApiResponse<ProfileUpdatePayload> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody MePatchRequest req
    ) {
        return ApiResponse.ok("프로필이 수정되었습니다.", userService.updateMe(principal.userId(), req));
    }

    @PatchMapping("/me/delete")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteMe(principal.userId());
        return ResponseEntity.noContent().build();
    }
}
