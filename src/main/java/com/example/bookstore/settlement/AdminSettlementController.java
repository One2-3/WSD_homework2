
package com.example.bookstore.settlement;

import com.example.bookstore.common.*;
import com.example.bookstore.settlement.dto.SettlementDtos.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settlements")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettlementController {

    private final SettlementService settlementService;

    public AdminSettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateSettlementResponse>> create(@Valid @RequestBody CreateSettlementRequest req) {
        var res = settlementService.create(req.period_start(), req.period_end());
        return ResponseEntity.ok(ApiResponse.ok("정산이 생성되었습니다.", res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<SettlementSummaryDto>>> list(
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ) {
        var pageable = PageableUtil.pageRequest(page, size, limit);
        Page<SettlementSummaryDto> result = settlementService.list(status, pageable).map(settlementService::toSummary);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<SettlementDetailDto>> detail(@PathVariable Long settlementId) {
        return ResponseEntity.ok(ApiResponse.ok("OK", settlementService.detail(settlementId)));
    }

    @PatchMapping("/{settlementId}/approve")
    public ResponseEntity<ApiResponse<SettlementSummaryDto>> approve(@PathVariable Long settlementId) {
        return ResponseEntity.ok(ApiResponse.ok("OK", settlementService.approve(settlementId)));
    }

    @PatchMapping("/{settlementId}/pay")
    public ResponseEntity<ApiResponse<SettlementSummaryDto>> pay(@PathVariable Long settlementId) {
        return ResponseEntity.ok(ApiResponse.ok("OK", settlementService.pay(settlementId)));
    }
}
