package com.example.bookstore.order;

import jakarta.persistence.*;

@Entity
@Table(name="order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id", nullable=false)
    private Long orderId;

    @Column(name="book_id", nullable=false)
    private Long bookId;

    @Column(name="seller_id", nullable=false)
    private Long sellerId;

    @Column(nullable=false)
    private Integer quantity;

    @Column(name="unit_price_cents", nullable=false)
    private Integer unitPriceCents;

    @Column(name="subtotal_cents", nullable=false)
    private Integer subtotalCents;

    public Long getId(){ return id; }
    public Long getOrderId(){ return orderId; }
    public void setOrderId(Long orderId){ this.orderId=orderId; }
    public Long getBookId(){ return bookId; }
    public void setBookId(Long bookId){ this.bookId=bookId; }
    public Long getSellerId(){ return sellerId; }
    public void setSellerId(Long sellerId){ this.sellerId=sellerId; }
    public Integer getQuantity(){ return quantity; }
    public void setQuantity(Integer quantity){ this.quantity=quantity; }
    public Integer getUnitPriceCents(){ return unitPriceCents; }
    public void setUnitPriceCents(Integer unitPriceCents){ this.unitPriceCents=unitPriceCents; }
    public Integer getSubtotalCents(){ return subtotalCents; }
    public void setSubtotalCents(Integer subtotalCents){ this.subtotalCents=subtotalCents; }
}
