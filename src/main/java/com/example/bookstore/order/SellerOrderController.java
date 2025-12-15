package com.example.bookstore.order;

import com.example.bookstore.common.ApiMeta;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ItemsPayload;
import com.example.bookstore.common.PageableUtil;
import com.example.bookstore.order.dto.SellerOrderDtos.*;
import com.example.bookstore.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/orders")
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    public SellerOrderController(SellerOrderService sellerOrderService) {
        this.sellerOrderService = sellerOrderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<SellerOrderItemDto>>> list(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ) {
        var pageable = PageableUtil.pageRequest(page, size, limit);
        Page<SellerOrderItemDto> result = sellerOrderService.list(me.userId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    
    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<java.util.Map<String,Object>>> ship(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long orderId
    ) {
        var status = sellerOrderService.ship(me.userId(), orderId);
        return ResponseEntity.ok(ApiResponse.ok("OK", java.util.Map.of("order_id", orderId, "status", status)));
    }

@GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<SellerOrderDetailDto>> detail(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", sellerOrderService.detail(me.userId(), orderId)));
    }
}
