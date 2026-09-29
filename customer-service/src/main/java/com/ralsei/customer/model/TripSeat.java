package com.ralsei.customer.model;

import java.math.BigDecimal;

import com.ralsei.customer.model.enums.TripSeatStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trip_seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Provides the trip seat component for the application.
 */
public class TripSeat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tripSeatId")
    private int tripSeatId;

    // [MICROSERVICE-REFACTOR]: Removed @ManyToOne Trip. Use scalar tripId + FeignClient.
    @Column(name = "tripId", nullable = false)
    private int tripId;

    // [MICROSERVICE-REFACTOR]: Removed @ManyToOne Seat. Use scalar seatId + FeignClient.
    @Column(name = "seatId", nullable = false)
    private int seatId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripSeatStatus status;
}
