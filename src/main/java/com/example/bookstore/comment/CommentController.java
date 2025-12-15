package com.example.bookstore.comment;

import com.example.bookstore.comment.dto.CommentDtos.*;
import com.example.bookstore.common.*;
import com.example.bookstore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // GET /api/reviews/{reviewId}/comments
    @GetMapping("/api/reviews/{reviewId}/comments")
    public ResponseEntity<ApiResponse<ItemsPayload<CommentDto>>> list(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long reviewId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ) {
        Pageable pageable = PageableUtil.pageRequest(page, size, limit);
        Page<CommentDto> result = commentService.list(me == null ? null : me.userId(), reviewId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    // POST /api/reviews/{reviewId}/comments
    @PostMapping("/api/reviews/{reviewId}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> create(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateCommentRequest req
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("댓글이 작성되었습니다.", commentService.create(me.userId(), reviewId, req)));
    }

    // PATCH /api/comments/{commentId}
    @PatchMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentDto>> patch(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long commentId,
            @Valid @RequestBody PatchCommentRequest req
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("댓글이 수정되었습니다.", commentService.patch(me.userId(), commentId, req)));
    }

    // DELETE /api/comments/{commentId}
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long commentId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        boolean isAdmin = me.role() != null && me.role().name().equalsIgnoreCase("admin");
        commentService.delete(me.userId(), isAdmin, commentId);
        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }

    // POST /api/comments/{commentId}/like
    @PostMapping("/api/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<LikePayload>> like(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long commentId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("댓글에 좋아요를 추가했습니다.", commentService.like(me.userId(), commentId)));
    }

    // DELETE /api/comments/{commentId}/like
    @DeleteMapping("/api/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<LikePayload>> unlike(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long commentId
    ) {
        if (me == null) throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        return ResponseEntity.ok(ApiResponse.ok("댓글 좋아요를 취소했습니다.", commentService.unlike(me.userId(), commentId)));
    }
}
