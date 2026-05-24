package com.tuanvm.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "coach")
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coachId")
    private int coachId;

    @Column(name = "seatLayoutId", nullable = false)
    private int seatLayoutId;

    @Column(name = "licensePlate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "year")
    private Integer year;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public Coach() {
    }

    public Coach(int coachId, int seatLayoutId, String licensePlate, String status, String manufacturer, Integer year,
            LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.coachId = coachId;
        this.seatLayoutId = seatLayoutId;
        this.licensePlate = licensePlate;
        this.status = status;
        this.manufacturer = manufacturer;
        this.year = year;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getCoachId() {
        return coachId;
    }

    public void setCoachId(int coachId) {
        this.coachId = coachId;
    }

    public int getSeatLayoutId() {
        return seatLayoutId;
    }

    public void setSeatLayoutId(int seatLayoutId) {
        this.seatLayoutId = seatLayoutId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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