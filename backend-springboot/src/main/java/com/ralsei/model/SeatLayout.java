package com.ralsei.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seat_layout")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayout extends BaseEntity {
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
}
