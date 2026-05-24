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
@Table(name = "cargo_ticket")
public class CargoTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTicketId")
    private int cargoTicketId;

    @Column(name = "tripId", nullable = false)
    private int tripId;

    @Column(name = "customerId")
    private Integer customerId;

    @Column(name = "senderName", nullable = false)
    private String senderName;

    @Column(name = "senderPhone", nullable = false)
    private String senderPhone;

    @Column(name = "senderEmail")
    private String senderEmail;

    @Column(name = "senderCccd", nullable = false)
    private String senderCccd;

    @Column(name = "receiverName", nullable = false)
    private String receiverName;

    @Column(name = "receiverPhone", nullable = false)
    private String receiverPhone;

    @Column(name = "receiverEmail")
    private String receiverEmail;

    @Column(name = "receiverCccd", nullable = false)
    private String receiverCccd;

    @Column(name = "ticketCode", nullable = false, unique = true)
    private String ticketCode;

    @Column(name = "totalPrice", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "description")
    private String description;

    @Column(name = "feePayer", nullable = false)
    private String feePayer;

    @Column(name = "codAmount", nullable = false)
    private BigDecimal codAmount;

    @Column(name = "pickupStopId", nullable = false)
    private int pickupStopId;

    @Column(name = "dropoffStopId", nullable = false)
    private int dropoffStopId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "soldBy", nullable = false)
    private int soldBy;

    @Column(name = "loadedBy")
    private Integer loadedBy;

    @Column(name = "unloadedBy")
    private Integer unloadedBy;

    @Column(name = "deliveredBy")
    private Integer deliveredBy;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public CargoTicket() {
    }

    public CargoTicket(int cargoTicketId, int tripId, Integer customerId, String senderName, String senderPhone,
            String senderEmail, String senderCccd, String receiverName, String receiverPhone, String receiverEmail,
            String receiverCccd, String ticketCode, BigDecimal totalPrice, String description, String feePayer,
            BigDecimal codAmount, int pickupStopId, int dropoffStopId, String status, int soldBy, Integer loadedBy,
            Integer unloadedBy, Integer deliveredBy, LocalDateTime createdAt, Integer createdBy,
            LocalDateTime updatedAt, Integer updatedBy) {
        this.cargoTicketId = cargoTicketId;
        this.tripId = tripId;
        this.customerId = customerId;
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.senderEmail = senderEmail;
        this.senderCccd = senderCccd;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverEmail = receiverEmail;
        this.receiverCccd = receiverCccd;
        this.ticketCode = ticketCode;
        this.totalPrice = totalPrice;
        this.description = description;
        this.feePayer = feePayer;
        this.codAmount = codAmount;
        this.pickupStopId = pickupStopId;
        this.dropoffStopId = dropoffStopId;
        this.status = status;
        this.soldBy = soldBy;
        this.loadedBy = loadedBy;
        this.unloadedBy = unloadedBy;
        this.deliveredBy = deliveredBy;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getCargoTicketId() {
        return cargoTicketId;
    }

    public void setCargoTicketId(int cargoTicketId) {
        this.cargoTicketId = cargoTicketId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderCccd() {
        return senderCccd;
    }

    public void setSenderCccd(String senderCccd) {
        this.senderCccd = senderCccd;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getReceiverCccd() {
        return receiverCccd;
    }

    public void setReceiverCccd(String receiverCccd) {
        this.receiverCccd = receiverCccd;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeePayer() {
        return feePayer;
    }

    public void setFeePayer(String feePayer) {
        this.feePayer = feePayer;
    }

    public BigDecimal getCodAmount() {
        return codAmount;
    }

    public void setCodAmount(BigDecimal codAmount) {
        this.codAmount = codAmount;
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

    public int getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(int soldBy) {
        this.soldBy = soldBy;
    }

    public Integer getLoadedBy() {
        return loadedBy;
    }

    public void setLoadedBy(Integer loadedBy) {
        this.loadedBy = loadedBy;
    }

    public Integer getUnloadedBy() {
        return unloadedBy;
    }

    public void setUnloadedBy(Integer unloadedBy) {
        this.unloadedBy = unloadedBy;
    }

    public Integer getDeliveredBy() {
        return deliveredBy;
    }

    public void setDeliveredBy(Integer deliveredBy) {
        this.deliveredBy = deliveredBy;
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