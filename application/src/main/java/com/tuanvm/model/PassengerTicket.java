package com.tuanvm.model;

import java.math.BigDecimal;
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
@Table(name = "passenger_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

   
}