package com.ralsei.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ralsei.model.enums.PassengerTicketMajorChangeType;
import com.ralsei.model.enums.PassengerTicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "passenger_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerTicket extends BaseEntity {
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

    @Column(name = "pickupStopName", nullable = false)
    private String pickupStopName;

    @Column(name = "dropoffStopName", nullable = false)
    private String dropoffStopName;

    @Column(name = "voucherCodeSnapshot")
    private String voucherCodeSnapshot;

    @Column(name = "refundPolicyDepartureTime")
    private LocalDateTime refundPolicyDepartureTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "majorChangeType")
    private PassengerTicketMajorChangeType majorChangeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PassengerTicketStatus status;

   
}