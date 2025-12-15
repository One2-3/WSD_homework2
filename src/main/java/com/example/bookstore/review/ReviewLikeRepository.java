package com.example.bookstore.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    void deleteByReviewId(Long reviewId);

    @Query("select rl.reviewId from ReviewLike rl where rl.userId = :userId and rl.reviewId in :reviewIds")
    List<Long> findLikedReviewIds(Long userId, Collection<Long> reviewIds);
}
