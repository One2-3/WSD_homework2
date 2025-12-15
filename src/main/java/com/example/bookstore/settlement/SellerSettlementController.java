package com.example.bookstore.settlement;

import com.example.bookstore.common.*;
import com.example.bookstore.security.UserPrincipal;
import com.example.bookstore.settlement.dto.SettlementDtos.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/seller/settlements")
@PreAuthorize("hasRole('SELLER')")
public class SellerSettlementController {

    private final SettlementService settlementService;

    public SellerSettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<SettlementSummaryDto>> list(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");

        Set<String> allowed = Set.of(
                "id", "createdAt", "updatedAt",
                "periodStart", "periodEnd",
                "status",
                "totalGrossCents", "totalCommissionCents", "totalNetCents"
        );
        Map<String, String> alias = Map.of(
                "created_at", "createdAt",
                "updated_at", "updatedAt",
                "period_start", "periodStart",
                "period_end", "periodEnd",
                "total_gross_cents", "totalGrossCents",
                "total_commission_cents", "totalCommissionCents",
                "total_net_cents", "totalNetCents"
        );

        Sort defaultSort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        Sort s = PageableUtil.parseSort(sort, defaultSort, allowed, alias);
        // tie-breaker 안전하게 추가
        if (s.getOrderFor("id") == null) s = s.and(Sort.by(Sort.Direction.DESC, "id"));

        PageRequest pageable = PageableUtil.pageRequest(page, size, limit, s);

        Page<SettlementSummaryDto> result = settlementService
        .listForSeller(me.userId(), status, pageable)
        .map(settlementService::toSummary); // 또는 SettlementService::toSummary
        return ResponseEntity.ok(PageResponse.from(result, pageable));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<SettlementDetailDto>> detail(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long settlementId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("OK", settlementService.detailForSeller(me.userId(), settlementId)));
    }

    @PatchMapping("/{settlementId}/confirm")
    public ResponseEntity<ApiResponse<SettlementSummaryDto>> confirm(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long settlementId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("OK", settlementService.sellerConfirm(me.userId(), settlementId)));
    }
}
