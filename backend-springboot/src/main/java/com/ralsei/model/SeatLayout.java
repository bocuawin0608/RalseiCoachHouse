package com.ralsei.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy="seatLayout", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SeatLayoutPrice> seatLayoutPrices;

    @OneToMany(mappedBy="seatLayout", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
}
