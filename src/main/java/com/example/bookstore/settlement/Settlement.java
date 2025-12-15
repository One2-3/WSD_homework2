
package com.example.bookstore.settlement;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="settlements",
       indexes = {
           @Index(name="idx_settlement_seller_period", columnList="seller_id,period_start,period_end"),
           @Index(name="idx_settlement_status", columnList="status")
       })
public class Settlement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="seller_id", nullable=false)
    private Long sellerId;

    @Column(name="period_start", nullable=false)
    private LocalDate periodStart;

    @Column(name="period_end", nullable=false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private SettlementStatus status = SettlementStatus.pending;

    @Column(name="total_gross_cents", nullable=false)
    private Integer totalGrossCents = 0;

    @Column(name="total_commission_cents", nullable=false)
    private Integer totalCommissionCents = 0;

    @Column(name="total_net_cents", nullable=false)
    private Integer totalNetCents = 0;

    @Column(name="paid_at")
    private Instant paidAt;

    @Column(columnDefinition = "text")
    private String note;

    @Column(name="seller_confirmed_at")
    private Instant sellerConfirmedAt;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = SettlementStatus.pending;
        if (totalGrossCents == null) totalGrossCents = 0;
        if (totalCommissionCents == null) totalCommissionCents = 0;
        if (totalNetCents == null) totalNetCents = 0;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }
    public Integer getTotalGrossCents() { return totalGrossCents; }
    public void setTotalGrossCents(Integer totalGrossCents) { this.totalGrossCents = totalGrossCents; }
    public Integer getTotalCommissionCents() { return totalCommissionCents; }
    public void setTotalCommissionCents(Integer totalCommissionCents) { this.totalCommissionCents = totalCommissionCents; }
    public Integer getTotalNetCents() { return totalNetCents; }
    public void setTotalNetCents(Integer totalNetCents) { this.totalNetCents = totalNetCents; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getSellerConfirmedAt() { return sellerConfirmedAt; }
    public void setSellerConfirmedAt(Instant sellerConfirmedAt) { this.sellerConfirmedAt = sellerConfirmedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
