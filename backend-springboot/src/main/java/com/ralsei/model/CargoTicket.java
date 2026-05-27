package com.ralsei.model;

import java.math.BigDecimal;

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
@Table(name = "cargo_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoTicket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTicketId")
    private int cargoTicketId;

    @Column(name = "tripId", nullable = false)
    private int tripId;

    @Column(name = "customerId")
    private Integer customerId;

    @Column(name = "senderName", nullable = false)
    private String senderName;

    @Column(name = "senderPhone", nullable = false)
    private String senderPhone;

    @Column(name = "senderEmail")
    private String senderEmail;

    @Column(name = "senderCccd", nullable = false)
    private String senderCccd;

    @Column(name = "receiverName", nullable = false)
    private String receiverName;

    @Column(name = "receiverPhone", nullable = false)
    private String receiverPhone;

    @Column(name = "receiverEmail")
    private String receiverEmail;

    @Column(name = "receiverCccd", nullable = false)
    private String receiverCccd;

    @Column(name = "ticketCode", nullable = false, unique = true)
    private String ticketCode;

    @Column(name = "totalPrice", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "description")
    private String description;

    @Column(name = "feePayer", nullable = false)
    private String feePayer;

    @Column(name = "codAmount", nullable = false)
    private BigDecimal codAmount;

    @Column(name = "pickupStopId", nullable = false)
    private int pickupStopId;

    @Column(name = "dropoffStopId", nullable = false)
    private int dropoffStopId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "soldBy", nullable = false)
    private int soldBy;

    @Column(name = "loadedBy")
    private Integer loadedBy;

    @Column(name = "unloadedBy")
    private Integer unloadedBy;

    @Column(name = "deliveredBy")
    private Integer deliveredBy;

}