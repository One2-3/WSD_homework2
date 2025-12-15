package com.example.bookstore.review;

import com.example.bookstore.book.BookRepository;
import com.example.bookstore.comment.CommentLikeRepository;
import com.example.bookstore.comment.CommentRepository;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.review.dto.ReviewDtos.*;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BookRepository bookRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewLikeRepository reviewLikeRepository,
            CommentRepository commentRepository,
            CommentLikeRepository commentLikeRepository,
            BookRepository bookRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    @CacheEvict(cacheNames = "topReviews", allEntries = true)
    public ReviewDto create(Long userId, Long bookId, CreateReviewRequest req) {
        bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 해당 도서에 리뷰를 작성했습니다.");
        }

        Review r = new Review();
        r.setUserId(userId);
        r.setBookId(bookId);
        r.setRating(req.rating());
        r.setBody(req.body());
        r.setLikeCount(0);

        try {
            Review saved = reviewRepository.save(r);
            refreshBookRatingStats(bookId);
            return toDto(saved, false);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 해당 도서에 리뷰를 작성했습니다.");
        }
    }

    @Transactional
    public Page<ReviewDto> listByBook(Long meIdOrNull, Long bookId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByBookId(bookId, pageable);

        Set<Long> liked = Collections.emptySet();
        if (meIdOrNull != null && !page.isEmpty()) {
            List<Long> ids = page.getContent().stream().map(Review::getId).toList();
            liked = new HashSet<>(reviewLikeRepository.findLikedReviewIds(meIdOrNull, ids));
        }
        final Set<Long> likedFinal = liked;

        return page.map(r -> toDto(r, likedFinal.contains(r.getId())));
    }

    @Transactional
    public ReviewDto detail(Long meIdOrNull, Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        boolean liked = meIdOrNull != null && reviewLikeRepository.existsByUserIdAndReviewId(meIdOrNull, reviewId);
        return toDto(r, liked);
    }

    @Transactional
    @CacheEvict(cacheNames = "topReviews", allEntries = true)
    public ReviewDto patch(Long userId, Long reviewId, PatchReviewRequest req) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!Objects.equals(r.getUserId(), userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }

        r.setRating(req.rating());
        r.setBody(req.body());
        Review saved = reviewRepository.save(r);

        refreshBookRatingStats(saved.getBookId());
        return toDto(saved, false);
    }

    @Transactional
    @CacheEvict(cacheNames = "topReviews", allEntries = true)
    public void delete(Long userId, boolean isAdmin, Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!isAdmin && !Objects.equals(r.getUserId(), userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
        }

        // 자식부터 정리
        var commentIds = commentRepository.findIdsByReviewId(reviewId);
        if (!commentIds.isEmpty()) {
            commentLikeRepository.deleteByCommentIdIn(commentIds);
            commentRepository.deleteByReviewId(reviewId);
        }

        reviewLikeRepository.deleteByReviewId(reviewId);
        reviewRepository.delete(r);

        refreshBookRatingStats(r.getBookId());
    }

    @Transactional
    @CacheEvict(cacheNames = "topReviews", allEntries = true)
    public LikePayload like(Long userId, Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            return new LikePayload(true, r.getLikeCount());
        }

        ReviewLike like = new ReviewLike();
        like.setUserId(userId);
        like.setReviewId(reviewId);

        try {
            reviewLikeRepository.save(like);
            r.setLikeCount(r.getLikeCount() + 1);
            reviewRepository.save(r);
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 이미 생겼을 수 있음 → 카운트는 올리지 않음
        }

        return new LikePayload(true, r.getLikeCount());
    }

    @Transactional
    @CacheEvict(cacheNames = "topReviews", allEntries = true)
    public LikePayload unlike(Long userId, Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        var existing = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId);
        if (existing.isEmpty()) {
            return new LikePayload(false, r.getLikeCount());
        }

        reviewLikeRepository.delete(existing.get());
        r.setLikeCount(Math.max(0, r.getLikeCount() - 1));
        reviewRepository.save(r);
        return new LikePayload(false, r.getLikeCount());
    }

    @Cacheable(cacheNames = "topReviews", key = "#bookId + ':' + #limit")
    @Transactional
    public TopReviewsPayload topReviews(Long bookId, int limit) {
        int l = Math.min(Math.max(limit, 1), 20);
        var pageable = org.springframework.data.domain.PageRequest.of(0, l);
        List<Review> list = reviewRepository.findTopByBookId(bookId, pageable);
        List<ReviewDto> items = list.stream().map(r -> toDto(r, false)).toList();
        return new TopReviewsPayload(items);
    }

    @Transactional
    public Page<ReviewDto> myReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable).map(r -> toDto(r, false));
    }

    private ReviewDto toDto(Review r, boolean liked) {
        return new ReviewDto(
                r.getId(),
                r.getUserId(),
                r.getBookId(),
                r.getRating(),
                r.getBody(),
                r.getLikeCount(),
                liked,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    private void refreshBookRatingStats(Long bookId) {
        bookRepository.refreshRatingStats(bookId);
    }
}
