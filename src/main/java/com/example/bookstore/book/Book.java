package com.example.bookstore.book;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "books",
        indexes = {
                @Index(name = "idx_books_seller", columnList = "seller_id"),
                @Index(name = "idx_books_deleted_at", columnList = "deleted_at")
        }
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="seller_id", nullable=false)
    private Long sellerId;

    @Column(nullable=false)
    private String title;

    @Column(name="price_cents", nullable=false)
    private Integer priceCents;

    @Column(nullable=false)
    private Integer stock;

    @Column(name="average_rating")
    private Double averageRating;

    @Column(name="ratings_count", nullable=false)
    private Integer ratingsCount = 0;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @Column(name="deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookAuthor> bookAuthors = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookCategory> bookCategories = new HashSet<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (ratingsCount == null) ratingsCount = 0;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public boolean isDeleted() { return deletedAt != null; }

    public Long getId() { return id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPriceCents() { return priceCents; }
    public void setPriceCents(Integer priceCents) { this.priceCents = priceCents; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getRatingsCount() { return ratingsCount; }
    public void setRatingsCount(Integer ratingsCount) { this.ratingsCount = ratingsCount; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Set<BookAuthor> getBookAuthors() { return bookAuthors; }
    public Set<BookCategory> getBookCategories() { return bookCategories; }
}
