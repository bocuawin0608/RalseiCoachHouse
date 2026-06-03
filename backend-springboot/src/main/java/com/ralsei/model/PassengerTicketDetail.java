package com.ralsei.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "passenger_ticket_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerTicketDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticketDetailId")
    private int ticketDetailId;

    @Column(name = "passengerTicketId", nullable = false)
    private int passengerTicketId;

    @Column(name = "tripSeatId", nullable = false)
    private int tripSeatId;

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

   
}