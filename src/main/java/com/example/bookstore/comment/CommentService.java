package com.example.bookstore.comment;

import com.example.bookstore.comment.dto.CommentDtos.*;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.review.ReviewRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ReviewRepository reviewRepository;

    public CommentService(CommentRepository commentRepository,
                          CommentLikeRepository commentLikeRepository,
                          ReviewRepository reviewRepository) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> list(Long meIdOrNull, Long reviewId, Pageable pageable) {
        // 리뷰 존재 확인(명세상 404)
        if (!reviewRepository.existsById(reviewId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }

        Page<Comment> page = commentRepository.findByReviewId(reviewId, pageable);

        Set<Long> liked = Collections.emptySet();
        if (meIdOrNull != null && !page.isEmpty()) {
            List<Long> ids = page.getContent().stream().map(Comment::getId).toList();
            liked = new HashSet<>(commentLikeRepository.findLikedCommentIds(meIdOrNull, ids));
        }
        final Set<Long> likedFinal = liked;

        return page.map(c -> toDto(c, likedFinal.contains(c.getId())));
    }

    @Transactional
    public CommentDto create(Long userId, Long reviewId, CreateCommentRequest req) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        }

        Comment c = new Comment();
        c.setUserId(userId);
        c.setReviewId(reviewId);
        c.setBody(req.body());
        c.setLikeCount(0);

        Comment saved = commentRepository.save(c);
        return toDto(saved, false);
    }

    @Transactional
    public CommentDto patch(Long userId, Long commentId, PatchCommentRequest req) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!Objects.equals(c.getUserId(), userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }

        c.setBody(req.body());
        Comment saved = commentRepository.save(c);
        return toDto(saved, false);
    }

    @Transactional
    public void delete(Long userId, boolean isAdmin, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!isAdmin && !Objects.equals(c.getUserId(), userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
        }

        commentLikeRepository.deleteByCommentId(commentId);
        commentRepository.delete(c);
    }

    @Transactional
    public LikePayload like(Long userId, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            return new LikePayload(true, c.getLikeCount());
        }

        CommentLike like = new CommentLike();
        like.setUserId(userId);
        like.setCommentId(commentId);

        try {
            commentLikeRepository.save(like);
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 이미 생겼을 수 있음
        }

        c.setLikeCount(c.getLikeCount() + 1);
        commentRepository.save(c);
        return new LikePayload(true, c.getLikeCount());
    }

    @Transactional
    public LikePayload unlike(Long userId, Long commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        var existing = commentLikeRepository.findByUserIdAndCommentId(userId, commentId);
        if (existing.isEmpty()) {
            return new LikePayload(false, c.getLikeCount());
        }

        commentLikeRepository.delete(existing.get());
        c.setLikeCount(Math.max(0, c.getLikeCount() - 1));
        commentRepository.save(c);
        return new LikePayload(false, c.getLikeCount());
    }

    private CommentDto toDto(Comment c, boolean liked) {
        return new CommentDto(
                c.getId(),
                c.getReviewId(),
                c.getUserId(),
                c.getBody(),
                c.getLikeCount(),
                liked,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
