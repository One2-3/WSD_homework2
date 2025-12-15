package com.example.bookstore.order;

import com.example.bookstore.common.*;
import com.example.bookstore.order.dto.OrderDtos.*;
import com.example.bookstore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 주문 생성(바로 구매) */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> create(
            @AuthenticationPrincipal UserPrincipal me,
            @Valid @RequestBody CreateOrderRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("주문이 생성되었습니다.", orderService.create(me.userId(), req.items())));
    }

    /** 장바구니 기반 주문 생성(결제) */
    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createFromCart(
            @AuthenticationPrincipal UserPrincipal me
    ) {
        return ResponseEntity.ok(ApiResponse.ok("주문이 생성되었습니다.", orderService.createFromCart(me.userId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<OrderSummaryDto>>> list(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ) {
        var pageable = PageableUtil.pageRequest(page, size, limit);
        Page<OrderSummaryDto> result = orderService.listMy(me.userId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    
    /** 주문 결제(테스트용) */
    @PatchMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderSummaryDto>> pay(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", orderService.payMy(me.userId(), orderId)));
    }

@GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailDto>> detail(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", orderService.detailMy(me.userId(), orderId)));
    }
}
