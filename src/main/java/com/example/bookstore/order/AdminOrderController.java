package com.example.bookstore.order;

import com.example.bookstore.common.*;
import com.example.bookstore.order.dto.OrderDtos.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<OrderSummaryDto>>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ) {
        var pageable = PageableUtil.pageRequest(page, size, limit);
        Page<OrderSummaryDto> result = orderService.adminList(pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<AdminPatchStatusResponse>> patchStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody AdminPatchStatusRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", orderService.adminPatchStatus(orderId, req.status())));
    }
}
