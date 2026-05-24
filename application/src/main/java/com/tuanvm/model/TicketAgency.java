package com.tuanvm.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "ticket_agency")
public class TicketAgency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticketAgencyId")
    private int ticketAgencyId;

    @Column(name = "stopPointId", nullable = false)
    private int stopPointId;

    @Column(name = "ticketAgencyName", nullable = false)
    private String ticketAgencyName;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public TicketAgency() {
    }

    public TicketAgency(int ticketAgencyId, int stopPointId, String ticketAgencyName, boolean isActive,
            LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.ticketAgencyId = ticketAgencyId;
        this.stopPointId = stopPointId;
        this.ticketAgencyName = ticketAgencyName;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getTicketAgencyId() {
        return ticketAgencyId;
    }

    public void setTicketAgencyId(int ticketAgencyId) {
        this.ticketAgencyId = ticketAgencyId;
    }

    public int getStopPointId() {
        return stopPointId;
    }

    public void setStopPointId(int stopPointId) {
        this.stopPointId = stopPointId;
    }

    public String getTicketAgencyName() {
        return ticketAgencyName;
    }

    public void setTicketAgencyName(String ticketAgencyName) {
        this.ticketAgencyName = ticketAgencyName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
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