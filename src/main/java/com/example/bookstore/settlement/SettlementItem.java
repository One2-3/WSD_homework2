
package com.example.bookstore.settlement;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="settlement_items",
       indexes = {
           @Index(name="idx_settlement_item_settlement", columnList="settlement_id"),
           @Index(name="idx_settlement_item_order_item", columnList="order_item_id"),
           @Index(name="idx_settlement_item_seller", columnList="seller_id")
       })
public class SettlementItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="settlement_id", nullable=false)
    private Long settlementId;

    @Column(name="order_item_id", nullable=false)
    private Long orderItemId;

    @Column(name="seller_id", nullable=false)
    private Long sellerId;

    @Column(name="gross_cents", nullable=false)
    private Integer grossCents;

    @Column(name="commission_cents", nullable=false)
    private Integer commissionCents;

    @Column(name="net_cents", nullable=false)
    private Integer netCents;

    @Column(name="created_at")
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public Integer getGrossCents() { return grossCents; }
    public void setGrossCents(Integer grossCents) { this.grossCents = grossCents; }
    public Integer getCommissionCents() { return commissionCents; }
    public void setCommissionCents(Integer commissionCents) { this.commissionCents = commissionCents; }
    public Integer getNetCents() { return netCents; }
    public void setNetCents(Integer netCents) { this.netCents = netCents; }
    public Instant getCreatedAt() { return createdAt; }
}
