package com.example.bookstore.sellers;

import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.sellers.dto.SellerAccountCreateRequest;
import com.example.bookstore.user.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ADMIN 전용: 판매자 로그인 계정 생성
 * POST /api/admin/sellers/{sellerId}/account
 */
@RestController
@RequestMapping("/api/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerAccountController {

    private final SellerAccountService sellerAccountService;

    public AdminSellerAccountController(SellerAccountService sellerAccountService) {
        this.sellerAccountService = sellerAccountService;
    }

    @PostMapping("/{sellerId}/account")
    public ApiResponse<UserDto> createAccount(
            @PathVariable long sellerId,
            @Valid @RequestBody SellerAccountCreateRequest req
    ) {
        var user = sellerAccountService.createSellerAccount(sellerId, req);
        return ApiResponse.ok("판매자 계정이 생성되었습니다.", UserDto.from(user));
    }
}
