package com.example.bookstore.cart;

import com.example.bookstore.cart.dto.CartDtos;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) { this.cartService = cartService; }

    @GetMapping
    public ResponseEntity<ApiResponse<CartDtos.CartView>> get(@AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(ApiResponse.ok("OK", cartService.getCartView(me.userId())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDtos.CartView>> add(
            @AuthenticationPrincipal UserPrincipal me,
            @Valid @RequestBody CartDtos.AddItemRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("장바구니에 추가되었습니다.",
                cartService.addItem(me.userId(), req.book_id(), req.quantity())));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDtos.CartView>> patch(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long itemId,
            @Valid @RequestBody CartDtos.PatchItemRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK",
                cartService.patchItemQty(me.userId(), itemId, req.quantity())));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long itemId
    ) {
        cartService.deleteItem(me.userId(), itemId);
        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clear(@AuthenticationPrincipal UserPrincipal me) {
        cartService.clear(me.userId());
        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }
}
