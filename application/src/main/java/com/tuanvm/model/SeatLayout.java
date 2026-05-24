package com.tuanvm.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "seat_layout")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seatLayoutId")
    private int seatLayoutId;

    @Column(name = "seatLayoutName", nullable = false)
    private String seatLayoutName;

    @Column(name = "totalSeat", nullable = false)
    private int totalSeat;

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


}