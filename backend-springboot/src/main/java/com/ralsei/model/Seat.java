package com.ralsei.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Provides the seat component for the application.
 */
public class Seat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seatId")
    private int seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachId", nullable = false)
    private Coach coach;

    @Builder.Default
    @OneToMany(mappedBy="seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TripSeat> tripSeats = new ArrayList<>();

    @Column(name = "seatCode", nullable = false)
    private String seatCode;

    @Column(name = "rowIndex", nullable = false)
    private int rowIndex;

    @Column(name = "colIndex", nullable = false)
    private int colIndex;

    @Column(name = "floorIndex", nullable = false)
    private int floorIndex;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;
}
