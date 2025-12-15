package com.example.bookstore.wishlist;

import com.example.bookstore.common.ApiMeta;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ItemsPayload;
import com.example.bookstore.common.PageableUtil;
import com.example.bookstore.security.UserPrincipal;
import com.example.bookstore.wishlist.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;
    public WishlistController(WishlistService wishlistService) { this.wishlistService = wishlistService; }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<WishlistItemDto>>> list(
        @AuthenticationPrincipal UserPrincipal me,
        @RequestParam(required=false) Integer page,
        @RequestParam(required=false) Integer size,
        @RequestParam(required=false) Integer limit
    ){
        var pageable = PageableUtil.pageRequest(page, size, limit);
        Page<WishlistItemDto> result = wishlistService.list(me.userId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistItemPayload>> add(
        @AuthenticationPrincipal UserPrincipal me,
        @Valid @RequestBody BookIdRequest req
    ){
        WishlistItemDto item = wishlistService.add(me.userId(), req.book_id());
        return ResponseEntity.ok(ApiResponse.ok("위시리스트에 추가되었습니다.", new WishlistItemPayload(item)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> remove(
        @AuthenticationPrincipal UserPrincipal me,
        @Valid @RequestBody BookIdRequest req
    ){
        wishlistService.remove(me.userId(), req.book_id());
        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }
}
