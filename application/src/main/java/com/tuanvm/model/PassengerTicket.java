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
@Table(name = "passenger_ticket")
public class PassengerTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passengerTicketId")
    private int passengerTicketId;

    @Column(name = "customerId")
    private Integer customerId;

    @Column(name = "tripId", nullable = false)
    private int tripId;

    @Column(name = "voucherId")
    private Integer voucherId;

    @Column(name = "soldBy")
    private Integer soldBy;

    @Column(name = "ticketCode", nullable = false, unique = true)
    private String ticketCode;

    @Column(name = "totalPrice", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "pickupStopId", nullable = false)
    private int pickupStopId;

    @Column(name = "dropoffStopId", nullable = false)
    private int dropoffStopId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public PassengerTicket() {
    }

    public PassengerTicket(int passengerTicketId, Integer customerId, int tripId, Integer voucherId, Integer soldBy,
            String ticketCode, BigDecimal totalPrice, int pickupStopId, int dropoffStopId, String status,
            LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.passengerTicketId = passengerTicketId;
        this.customerId = customerId;
        this.tripId = tripId;
        this.voucherId = voucherId;
        this.soldBy = soldBy;
        this.ticketCode = ticketCode;
        this.totalPrice = totalPrice;
        this.pickupStopId = pickupStopId;
        this.dropoffStopId = dropoffStopId;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getPassengerTicketId() {
        return passengerTicketId;
    }

    public void setPassengerTicketId(int passengerTicketId) {
        this.passengerTicketId = passengerTicketId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public Integer getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(Integer soldBy) {
        this.soldBy = soldBy;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getPickupStopId() {
        return pickupStopId;
    }

    public void setPickupStopId(int pickupStopId) {
        this.pickupStopId = pickupStopId;
    }

    public int getDropoffStopId() {
        return dropoffStopId;
    }

    public void setDropoffStopId(int dropoffStopId) {
        this.dropoffStopId = dropoffStopId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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