package com.example.bookstore.cart;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = @UniqueConstraint(name="uk_cart_book", columnNames={"cart_id","book_id"})
)
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="cart_id", nullable=false)
    private Long cartId;

    @Column(name="book_id", nullable=false)
    private Long bookId;

    @Column(nullable=false)
    private Integer quantity;

    @Column(name="unit_price_cents", nullable=false)
    private Integer unitPriceCents;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public Long getId(){ return id; }
    public Long getCartId(){ return cartId; }
    public void setCartId(Long cartId){ this.cartId=cartId; }
    public Long getBookId(){ return bookId; }
    public void setBookId(Long bookId){ this.bookId=bookId; }
    public Integer getQuantity(){ return quantity; }
    public void setQuantity(Integer quantity){ this.quantity=quantity; }
    public Integer getUnitPriceCents(){ return unitPriceCents; }
    public void setUnitPriceCents(Integer unitPriceCents){ this.unitPriceCents=unitPriceCents; }
}
