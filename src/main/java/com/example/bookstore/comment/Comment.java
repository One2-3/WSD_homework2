package com.example.bookstore.comment;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="review_id", nullable=false)
    private Long reviewId;

    @Column(name="user_id")
    private Long userId;

    @Column(nullable=false, columnDefinition="text")
    private String body;

    @Column(name="like_count", nullable=false)
    private Integer likeCount = 0;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (likeCount == null) likeCount = 0;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public Long getId(){ return id; }
    public Long getReviewId(){ return reviewId; }
    public void setReviewId(Long reviewId){ this.reviewId=reviewId; }
    public Long getUserId(){ return userId; }
    public void setUserId(Long userId){ this.userId=userId; }
    public String getBody(){ return body; }
    public void setBody(String body){ this.body=body; }
    public Integer getLikeCount(){ return likeCount; }
    public void setLikeCount(Integer likeCount){ this.likeCount=likeCount; }
    public Instant getCreatedAt(){ return createdAt; }
    public Instant getUpdatedAt(){ return updatedAt; }
}
