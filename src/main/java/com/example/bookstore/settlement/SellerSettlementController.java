package com.example.bookstore.settlement;

import com.example.bookstore.common.*;
import com.example.bookstore.security.UserPrincipal;
import com.example.bookstore.settlement.dto.SettlementDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/settlements")
@PreAuthorize("hasRole('SELLER')")
public class SellerSettlementController {

    private final SettlementService settlementService;

    public SellerSettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    /**
     * 판매자 정산 확인
     */
    @PatchMapping("/{settlementId}/confirm")
    public ResponseEntity<ApiResponse<SettlementSummaryDto>> confirm(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long settlementId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        SettlementSummaryDto dto = settlementService.sellerConfirm(me.userId(), settlementId);
        return ResponseEntity.ok(ApiResponse.ok("OK", dto));
    }

    /**
     * 판매자 정산 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<SettlementSummaryDto>>> list(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        var pageable = PageableUtil.pageRequest(page, size, limit);
        Page<SettlementSummaryDto> result = settlementService
                .listForSeller(me.userId(), status, pageable)
                .map(settlementService::toSummary);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    /**
     * 판매자 정산 상세
     */
    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<SettlementDetailDto>> detail(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long settlementId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("OK", settlementService.detailForSeller(me.userId(), settlementId)));
    }
}
