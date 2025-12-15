package com.example.bookstore.order;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private OrderStatus status = OrderStatus.pending;

    @Column(name="total_amount_cents", nullable=false)
    private Integer totalAmountCents = 0;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = OrderStatus.pending;
        if (totalAmountCents == null) totalAmountCents = 0;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public Long getId(){ return id; }
    public Long getUserId(){ return userId; }
    public void setUserId(Long userId){ this.userId=userId; }
    public OrderStatus getStatus(){ return status; }
    public void setStatus(OrderStatus status){ this.status=status; }
    public Integer getTotalAmountCents(){ return totalAmountCents; }
    public void setTotalAmountCents(Integer totalAmountCents){ this.totalAmountCents=totalAmountCents; }
    public Instant getCreatedAt(){ return createdAt; }
    public Instant getUpdatedAt(){ return updatedAt; }
}
