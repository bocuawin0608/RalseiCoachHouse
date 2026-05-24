package com.tuanvm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "passenger_ticket_detail")
public class PassengerTicketDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticketDetailId")
    private int ticketDetailId;

    @Column(name = "passengerTicketId", nullable = false)
    private int passengerTicketId;

    @Column(name = "seatId", nullable = false)
    private int seatId;

    @Column(name = "qrcode", columnDefinition = "VARCHAR(MAX)")
    private String qrcode;

    @Column(name = "fullName", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "cccd", nullable = false)
    private String cccd;

    @Column(name = "email")
    private String email;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "expiredAt")
    private LocalDateTime expiredAt;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public PassengerTicketDetail() {
    }

    public PassengerTicketDetail(int ticketDetailId, int passengerTicketId, int seatId, String qrcode,
            String fullName, String phone, LocalDate dob, String cccd, String email, BigDecimal price, String status,
            LocalDateTime expiredAt, LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt,
            Integer updatedBy) {
        this.ticketDetailId = ticketDetailId;
        this.passengerTicketId = passengerTicketId;
        this.seatId = seatId;
        this.qrcode = qrcode;
        this.fullName = fullName;
        this.phone = phone;
        this.dob = dob;
        this.cccd = cccd;
        this.email = email;
        this.price = price;
        this.status = status;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getTicketDetailId() {
        return ticketDetailId;
    }

    public void setTicketDetailId(int ticketDetailId) {
        this.ticketDetailId = ticketDetailId;
    }

    public int getPassengerTicketId() {
        return passengerTicketId;
    }

    public void setPassengerTicketId(int passengerTicketId) {
        this.passengerTicketId = passengerTicketId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
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