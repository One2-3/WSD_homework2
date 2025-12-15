package com.example.bookstore.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByReviewId(Long reviewId, Pageable pageable);

    void deleteByReviewId(Long reviewId);

    @Query("select c.id from Comment c where c.reviewId = :reviewId")
    List<Long> findIdsByReviewId(Long reviewId);
}
