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
@Table(name = "cargo_ticket_detail")
public class CargoTicketDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTicketDetailId")
    private int cargoTicketDetailId;

    @Column(name = "cargoTicketId", nullable = false)
    private int cargoTicketId;

    @Column(name = "cargoTypePriceId", nullable = false)
    private int cargoTypePriceId;

    @Column(name = "description")
    private String description;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "weightKg", nullable = false)
    private BigDecimal weightKg;

    @Column(name = "dimensionVol", nullable = false)
    private BigDecimal dimensionVol;

    @Column(name = "calculatedPrice", nullable = false)
    private BigDecimal calculatedPrice;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public CargoTicketDetail() {
    }

    public CargoTicketDetail(int cargoTicketDetailId, int cargoTicketId, int cargoTypePriceId, String description,
            int quantity, BigDecimal weightKg, BigDecimal dimensionVol, BigDecimal calculatedPrice,
            LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.cargoTicketDetailId = cargoTicketDetailId;
        this.cargoTicketId = cargoTicketId;
        this.cargoTypePriceId = cargoTypePriceId;
        this.description = description;
        this.quantity = quantity;
        this.weightKg = weightKg;
        this.dimensionVol = dimensionVol;
        this.calculatedPrice = calculatedPrice;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getCargoTicketDetailId() {
        return cargoTicketDetailId;
    }

    public void setCargoTicketDetailId(int cargoTicketDetailId) {
        this.cargoTicketDetailId = cargoTicketDetailId;
    }

    public int getCargoTicketId() {
        return cargoTicketId;
    }

    public void setCargoTicketId(int cargoTicketId) {
        this.cargoTicketId = cargoTicketId;
    }

    public int getCargoTypePriceId() {
        return cargoTypePriceId;
    }

    public void setCargoTypePriceId(int cargoTypePriceId) {
        this.cargoTypePriceId = cargoTypePriceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getDimensionVol() {
        return dimensionVol;
    }

    public void setDimensionVol(BigDecimal dimensionVol) {
        this.dimensionVol = dimensionVol;
    }

    public BigDecimal getCalculatedPrice() {
        return calculatedPrice;
    }

    public void setCalculatedPrice(BigDecimal calculatedPrice) {
        this.calculatedPrice = calculatedPrice;
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