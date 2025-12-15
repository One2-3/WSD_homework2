package com.example.bookstore.sellers;

import com.example.bookstore.common.ApiMeta;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ItemsPayload;
import com.example.bookstore.sellers.dto.SellerCreateRequest;
import com.example.bookstore.sellers.dto.SellerDto;
import com.example.bookstore.sellers.dto.SellerListItemDto;
import com.example.bookstore.sellers.dto.SellerPatchRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADMIN 전용 판매자 관리 API.
 * - List 응답은 payload.items + (root) meta 형태로 통일.
 */
@RestController
@RequestMapping("/api/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /** 판매자 생성 */
    @PostMapping
    public ApiResponse<SellerDto> create(@Valid @RequestBody SellerCreateRequest req) {
        Seller created = sellerService.create(req);
        return ApiResponse.ok("판매자가 생성되었습니다.", SellerDto.from(created));
    }

    /** 판매자 목록 (page는 1-base) */
    @GetMapping
    public ApiResponse<ItemsPayload<SellerListItemDto>> list(
            @RequestParam(required = false) SellerStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<Seller> result = sellerService.list(status, page, limit);
        List<SellerListItemDto> items = result.map(SellerListItemDto::from).getContent();
        ApiMeta meta = ApiMeta.fromPage(result);
        return ApiResponse.ok("OK", new ItemsPayload<>(items), meta);
    }

    /** 판매자 상세 */
    @GetMapping("/{sellerId}")
    public ApiResponse<SellerDto> detail(@PathVariable long sellerId) {
        Seller s = sellerService.get(sellerId);
        return ApiResponse.ok("OK", SellerDto.from(s));
    }

    /** 판매자 수정 */
    @PatchMapping("/{sellerId}")
    public ApiResponse<SellerDto> patch(
            @PathVariable long sellerId,
            @Valid @RequestBody SellerPatchRequest req
    ) {
        Seller s = sellerService.patch(sellerId, req);
        return ApiResponse.ok("판매자가 수정되었습니다.", SellerDto.from(s));
    }

    /** 판매자 삭제(소프트) */
    @DeleteMapping("/{sellerId}")
    public ResponseEntity<Void> softDelete(@PathVariable long sellerId) {
        sellerService.softDelete(sellerId);
        return ResponseEntity.noContent().build();
    }

    /** 판매자 삭제(하드) */
    @DeleteMapping("/{sellerId}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable long sellerId) {
        sellerService.hardDelete(sellerId);
        return ResponseEntity.noContent().build();
    }
}
