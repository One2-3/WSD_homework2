package com.example.bookstore.sellers;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.security.UserPrincipal;
import com.example.bookstore.sellers.dto.SellerDto;
import com.example.bookstore.sellers.dto.SellerSelfPatchRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/me")
@PreAuthorize("hasRole('SELLER')")
public class SellerMeController {

    private final SellerSelfService sellerSelfService;

    public SellerMeController(SellerSelfService sellerSelfService) {
        this.sellerSelfService = sellerSelfService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SellerDto>> me(@AuthenticationPrincipal UserPrincipal me) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        Seller s = sellerSelfService.getSelf(me.userId());
        return ResponseEntity.ok(ApiResponse.ok("OK", SellerDto.from(s)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<SellerDto>> patch(
            @AuthenticationPrincipal UserPrincipal me,
            @Valid @RequestBody SellerSelfPatchRequest req
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        Seller s = sellerSelfService.patchSelf(me.userId(), req);
        return ResponseEntity.ok(ApiResponse.ok("OK", SellerDto.from(s)));
    }
}
