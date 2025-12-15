package com.example.bookstore.book;

import com.example.bookstore.book.dto.BookDetailDto;
import com.example.bookstore.book.dto.BookPatchRequest;
import com.example.bookstore.book.dto.BookSummaryDto;
import com.example.bookstore.book.dto.SellerBookCreateRequest;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ApiMeta;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.common.ItemsPayload;
import com.example.bookstore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seller/books")
@PreAuthorize("hasRole('SELLER')")
public class SellerBookController {

    private final BookService bookService;

    public SellerBookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/seller/books (판매자 본인 도서 목록)
    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<BookSummaryDto>>> myBooks(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        PageRequest pageable = pageRequest(page, size, limit, sort);
        Page<BookSummaryDto> result = bookService.listForSeller(me.userId(), q, pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    // POST /api/seller/books (판매자 도서 등록)
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @AuthenticationPrincipal UserPrincipal me,
            @Valid @RequestBody SellerBookCreateRequest req
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        Book b = bookService.createForSeller(me.userId(), req);
        return ResponseEntity.ok(ApiResponse.ok("도서가 등록되었습니다.", Map.of("book_id", b.getId())));
    }

    // PATCH /api/seller/books/{bookId} (판매자 도서 수정)
    @PatchMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long bookId,
            @Valid @RequestBody BookPatchRequest req
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        Book b = bookService.patchForSeller(me.userId(), bookId, req);
        BookDetailDto dto = bookService.toDetail(b);
        return ResponseEntity.ok(ApiResponse.ok("수정됨.", Map.of("book", dto)));
    }

    // DELETE /api/seller/books/{bookId} (판매자 도서 삭제 - 소프트)
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long bookId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        bookService.softDeleteForSeller(me.userId(), bookId);
        return ResponseEntity.noContent().build();
    }

    /**
     * sort 예시: sort=price_cents,DESC or sort=updated_at,ASC
     * - 허용 필드: id, price_cents, updated_at, average_rating, ratings_count
     */
    private PageRequest pageRequest(Integer page, Integer size, Integer limit, String sort) {
        int p = (page == null ? 1 : Math.max(page, 1)) - 1;   // 1-base
        Integer raw = (size != null ? size : limit);
        int l = (raw == null ? 20 : Math.min(Math.max(raw, 1), 100));
        return PageRequest.of(p, l, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "sort 형식이 올바르지 않습니다.");
        }
        String fieldRaw = parts[0].trim();
        String dirRaw = parts[1].trim().toUpperCase();

        String field = switch (fieldRaw) {
            case "id" -> "id";
            case "price_cents" -> "priceCents";
            case "updated_at" -> "updatedAt";
            case "average_rating" -> "averageRating";
            case "ratings_count" -> "ratingsCount";
            default -> throw new ApiException(ErrorCode.VALIDATION_FAILED, "정렬 필드가 올바르지 않습니다.");
        };

        Sort.Direction dir;
        try {
            dir = Sort.Direction.valueOf(dirRaw);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "정렬 방향이 올바르지 않습니다.");
        }

        return Sort.by(dir, field);
    }
}
