package com.example.bookstore.book;

import com.example.bookstore.book.dto.BookDetailDto;
import com.example.bookstore.book.dto.BookSummaryDto;
import com.example.bookstore.common.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books?q=&seller_id=&author_id=&category_id=&page=&limit=&sort=
    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<BookSummaryDto>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(name = "seller_id", required = false) Long sellerId,
            @RequestParam(name = "author_id", required = false) Long authorId,
            @RequestParam(name = "category_id", required = false) Long categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort
    ) {
        PageRequest pageable = pageRequest(page, size, limit, sort);
        Page<BookSummaryDto> result = bookService.listPublic(q, sellerId, authorId, categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    // GET /api/books/{bookId}
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detail(@PathVariable Long bookId) {
        BookDetailDto dto = bookService.detail(bookId);
        return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("book", dto)));
    }

    /**
     * sort 예시: sort=price_cents,DESC or sort=updated_at,ASC
     * - 허용 필드: id, price_cents, updated_at, average_rating, ratings_count
     */
    private PageRequest pageRequest(Integer page, Integer size, Integer limit, String sort) {
        int p = (page == null ? 1 : Math.max(page, 1)) - 1;   // 1-base
        Integer raw = (size != null ? size : limit);
        int l = (raw == null ? 20 : Math.min(Math.max(raw, 1), 100));

        Sort s = parseSort(sort);
        return PageRequest.of(p, l, s);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "sort 형식이 올바르지 않습니다.",
                    Map.of("sort", "expected field,ASC|DESC"));
        }

        String fieldRaw = parts[0].trim();
        String dirRaw = parts[1].trim().toUpperCase();

        String field = switch (fieldRaw) {
            case "id" -> "id";
            case "price_cents" -> "priceCents";
            case "updated_at" -> "updatedAt";
            case "average_rating" -> "averageRating";
            case "ratings_count" -> "ratingsCount";
            default -> throw new ApiException(ErrorCode.VALIDATION_FAILED, "정렬 필드가 올바르지 않습니다.",
                    Map.of("sort", "unsupported field: " + fieldRaw));
        };

        Sort.Direction dir;
        try {
            dir = Sort.Direction.valueOf(dirRaw);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "정렬 방향이 올바르지 않습니다.",
                    Map.of("sort", "direction must be ASC or DESC"));
        }

        return Sort.by(dir, field);
    }
}
