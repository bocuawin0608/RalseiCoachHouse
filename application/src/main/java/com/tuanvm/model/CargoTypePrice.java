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
@Table(name = "cargo_type_price")
public class CargoTypePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTypePriceId")
    private int cargoTypePriceId;

    @Column(name = "cargoTypeId", nullable = false)
    private int cargoTypeId;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "pricePerUnit", nullable = false)
    private BigDecimal pricePerUnit;

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

    public CargoTypePrice() {
    }

    public CargoTypePrice(int cargoTypePriceId, int cargoTypeId, String unit, BigDecimal pricePerUnit,
            LocalDateTime startEffectiveDate, LocalDateTime endEffectiveDate, LocalDateTime createdAt,
            Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.cargoTypePriceId = cargoTypePriceId;
        this.cargoTypeId = cargoTypeId;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.startEffectiveDate = startEffectiveDate;
        this.endEffectiveDate = endEffectiveDate;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getCargoTypePriceId() {
        return cargoTypePriceId;
    }

    public void setCargoTypePriceId(int cargoTypePriceId) {
        this.cargoTypePriceId = cargoTypePriceId;
    }

    public int getCargoTypeId() {
        return cargoTypeId;
    }

    public void setCargoTypeId(int cargoTypeId) {
        this.cargoTypeId = cargoTypeId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
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