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
@Table(name = "accompanied_child")
public class AccompaniedChild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accompaniedChildId")
    private int accompaniedChildId;

    @Column(name = "ticketDetailId", nullable = false, unique = true)
    private int ticketDetailId;

    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    public AccompaniedChild() {
    }

    public AccompaniedChild(int accompaniedChildId, int ticketDetailId, String fullname, LocalDate dob,
            LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt, Integer updatedBy) {
        this.accompaniedChildId = accompaniedChildId;
        this.ticketDetailId = ticketDetailId;
        this.fullname = fullname;
        this.dob = dob;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getAccompaniedChildId() {
        return accompaniedChildId;
    }

    public void setAccompaniedChildId(int accompaniedChildId) {
        this.accompaniedChildId = accompaniedChildId;
    }

    public int getTicketDetailId() {
        return ticketDetailId;
    }

    public void setTicketDetailId(int ticketDetailId) {
        this.ticketDetailId = ticketDetailId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
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