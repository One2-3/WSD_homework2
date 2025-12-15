package com.example.bookstore.review;

import com.example.bookstore.common.*;
import com.example.bookstore.review.dto.ReviewDtos.*;
import com.example.bookstore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // GET /api/books/{bookId}/reviews
    @GetMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<ItemsPayload<ReviewDto>>> listByBook(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long bookId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false, defaultValue = "latest") String sort
    ) {
        Pageable pageable = ReviewSort.pageable(page, size, limit, sort);
        Page<ReviewDto> result = reviewService.listByBook(me == null ? null : me.userId(), bookId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    // POST /api/books/{bookId}/reviews
    @PostMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<ReviewDto>> create(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long bookId,
            @Valid @RequestBody CreateReviewRequest req
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("리뷰가 작성되었습니다.", reviewService.create(me.userId(), bookId, req)));
    }

    // GET /api/reviews/{reviewId}
    @GetMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> detail(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", reviewService.detail(me == null ? null : me.userId(), reviewId)));
    }

    // PATCH /api/reviews/{reviewId}
    @PatchMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> patch(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long reviewId,
            @Valid @RequestBody PatchReviewRequest req
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("리뷰가 수정되었습니다.", reviewService.patch(me.userId(), reviewId, req)));
    }

    // DELETE /api/reviews/{reviewId}
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long reviewId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        boolean isAdmin = me.role() != null && me.role().name().equalsIgnoreCase("admin");
        reviewService.delete(me.userId(), isAdmin, reviewId);
        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }

    // POST /api/reviews/{reviewId}/like
    @PostMapping("/api/reviews/{reviewId}/like")
    public ResponseEntity<ApiResponse<LikePayload>> like(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long reviewId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("리뷰에 좋아요를 추가했습니다.", reviewService.like(me.userId(), reviewId)));
    }

    // DELETE /api/reviews/{reviewId}/like
    @DeleteMapping("/api/reviews/{reviewId}/like")
    public ResponseEntity<ApiResponse<LikePayload>> unlike(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long reviewId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("리뷰 좋아요를 취소했습니다.", reviewService.unlike(me.userId(), reviewId)));
    }

    // GET /api/books/{bookId}/reviews/top?limit=3
    @GetMapping("/api/books/{bookId}/reviews/top")
    public ResponseEntity<ApiResponse<TopReviewsPayload>> top(
            @PathVariable Long bookId,
            @RequestParam(required = false, defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", reviewService.topReviews(bookId, limit)));
    }

    // GET /api/users/me/reviews
    @GetMapping("/api/users/me/reviews")
    public ResponseEntity<ApiResponse<ItemsPayload<ReviewDto>>> myReviews(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false, defaultValue = "latest") String sort
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        Pageable pageable = ReviewSort.pageable(page, size, limit, sort);
        var result = reviewService.myReviews(me.userId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }
}
