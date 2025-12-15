package com.example.bookstore.review;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="review_likes",
        uniqueConstraints = @UniqueConstraint(name="uk_review_like_user_review", columnNames={"user_id","review_id"}))
public class ReviewLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="review_id", nullable=false)
    private Long reviewId;

    @Column(name="created_at")
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Instant getCreatedAt() { return createdAt; }
}
