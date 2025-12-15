package com.example.bookstore.wishlist;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "wishlist_items",
    uniqueConstraints = @UniqueConstraint(name="uk_wishlist_user_book", columnNames={"user_id","book_id"})
)
public class WishlistItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="book_id", nullable=false)
    private Long bookId;

    @Column(name="created_at")
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId(){ return id; }
    public Long getUserId(){ return userId; }
    public void setUserId(Long userId){ this.userId=userId; }
    public Long getBookId(){ return bookId; }
    public void setBookId(Long bookId){ this.bookId=bookId; }
    public Instant getCreatedAt(){ return createdAt; }
}
