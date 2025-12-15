package com.example.bookstore.book;

import com.example.bookstore.book.dto.BookCreateRequest;
import com.example.bookstore.book.dto.BookDetailDto;
import com.example.bookstore.book.dto.BookPatchRequest;
import com.example.bookstore.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/books")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * POST /api/admin/books  (도서 등록 - 관리자)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@Valid @RequestBody BookCreateRequest req) {
        Book b = bookService.createAdmin(req);
        var payload = java.util.Map.of(
                "book", java.util.Map.of(
                        "id", b.getId(),
                        "seller_id", b.getSellerId(),
                        "title", b.getTitle(),
                        "price_cents", b.getPriceCents(),
                        "stock", b.getStock(),
                        "average_rating", b.getAverageRating(),
                        "ratings_count", b.getRatingsCount()
                )
        );
        return ResponseEntity.ok(ApiResponse.success("도서가 등록되었습니다.", payload));
    }

    /**
     * PATCH /api/admin/books/{bookId}  (도서 수정 - 관리자)
     */
    @PatchMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Object>> patch(@PathVariable Long bookId,
                                                     @Valid @RequestBody BookPatchRequest req) {
        Book b = bookService.patchAdmin(bookId, req);
        BookDetailDto dto = bookService.toDetail(b);

        var payload = java.util.Map.of(
                "book", java.util.Map.of(
                        "id", dto.id(),
                        "seller_id", dto.seller_id(),
                        "title", dto.title(),
                        "price_cents", dto.price_cents(),
                        "stock", dto.stock(),
                        "average_rating", dto.average_rating(),
                        "ratings_count", dto.ratings_count(),
                        "authors", dto.authors(),
                        "categories", dto.categories(),
                        "updated_at", dto.updated_at()
                )
        );

        return ResponseEntity.ok(ApiResponse.success("수정됨.", payload));
    }

    /**
     * DELETE /api/admin/books/{bookId}  (도서 삭제 - 관리자, 소프트 삭제)
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(@PathVariable Long bookId) {
        bookService.softDeleteAdmin(bookId);
        return ResponseEntity.noContent().build();
    }
}
