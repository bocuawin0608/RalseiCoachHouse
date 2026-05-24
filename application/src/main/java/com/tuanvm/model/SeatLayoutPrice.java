package com.tuanvm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "seat_layout_price")
public class SeatLayoutPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seatLayoutPriceId")
    private int seatLayoutPriceId;

    @Column(name = "seatLayoutId", nullable = false)
    private int seatLayoutId;

    @Column(name = "seatPrice", nullable = false)
    private BigDecimal seatPrice;

    @Column(name = "startEffectiveDate", nullable = false)
    private LocalDateTime startEffectiveDate;

    @Column(name = "endEffectiveDate", nullable = false)
    private LocalDateTime endEffectiveDate;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public SeatLayoutPrice() {
    }

    public SeatLayoutPrice(int seatLayoutPriceId, int seatLayoutId, BigDecimal seatPrice,
            LocalDateTime startEffectiveDate, LocalDateTime endEffectiveDate, LocalDateTime createdAt,
            Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.seatLayoutPriceId = seatLayoutPriceId;
        this.seatLayoutId = seatLayoutId;
        this.seatPrice = seatPrice;
        this.startEffectiveDate = startEffectiveDate;
        this.endEffectiveDate = endEffectiveDate;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getSeatLayoutPriceId() {
        return seatLayoutPriceId;
    }

    public void setSeatLayoutPriceId(int seatLayoutPriceId) {
        this.seatLayoutPriceId = seatLayoutPriceId;
    }

    public int getSeatLayoutId() {
        return seatLayoutId;
    }

    public void setSeatLayoutId(int seatLayoutId) {
        this.seatLayoutId = seatLayoutId;
    }

    public BigDecimal getSeatPrice() {
        return seatPrice;
    }

    public void setSeatPrice(BigDecimal seatPrice) {
        this.seatPrice = seatPrice;
    }

    public LocalDateTime getStartEffectiveDate() {
        return startEffectiveDate;
    }

    public void setStartEffectiveDate(LocalDateTime startEffectiveDate) {
        this.startEffectiveDate = startEffectiveDate;
    }

    public LocalDateTime getEndEffectiveDate() {
        return endEffectiveDate;
    }

    public void setEndEffectiveDate(LocalDateTime endEffectiveDate) {
        this.endEffectiveDate = endEffectiveDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }
}