package com.example.bookstore.comment;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="comment_likes",
        uniqueConstraints = @UniqueConstraint(name="uk_comment_like_user_comment", columnNames={"user_id","comment_id"}))
public class CommentLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="comment_id", nullable=false)
    private Long commentId;

    @Column(name="created_at")
    private Instant createdAt;

    @PrePersist
    void prePersist(){ if (createdAt == null) createdAt = Instant.now(); }

    public Long getId(){ return id; }
    public Long getUserId(){ return userId; }
    public void setUserId(Long userId){ this.userId=userId; }
    public Long getCommentId(){ return commentId; }
    public void setCommentId(Long commentId){ this.commentId=commentId; }
    public Instant getCreatedAt(){ return createdAt; }
}
