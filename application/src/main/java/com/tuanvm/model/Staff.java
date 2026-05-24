package com.tuanvm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staffId")
    private int staffId;

    @Column(name = "accountId", unique = true)
    private Integer accountId;

    @Column(name = "ticketAgencyId")
    private Integer ticketAgencyId;

    @Column(name = "staffName", nullable = false)
    private String staffName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "staffPosition", nullable = false)
    private String staffPosition;

    @Column(name = "hireDate", nullable = false)
    private LocalDate hireDate;

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

    public Staff() {
    }

    public Staff(int staffId, Integer accountId, Integer ticketAgencyId, String staffName, String phone, String email,
            LocalDate dob, String cccd, String staffPosition, LocalDate hireDate, boolean isActive,
            LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.staffId = staffId;
        this.accountId = accountId;
        this.ticketAgencyId = ticketAgencyId;
        this.staffName = staffName;
        this.phone = phone;
        this.email = email;
        this.dob = dob;
        this.cccd = cccd;
        this.staffPosition = staffPosition;
        this.hireDate = hireDate;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getTicketAgencyId() {
        return ticketAgencyId;
    }

    public void setTicketAgencyId(Integer ticketAgencyId) {
        this.ticketAgencyId = ticketAgencyId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getStaffPosition() {
        return staffPosition;
    }

    public void setStaffPosition(String staffPosition) {
        this.staffPosition = staffPosition;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
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