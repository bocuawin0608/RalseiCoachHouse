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
@Table(name = "cargo_ticket_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoTicketDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTicketDetailId")
    private int cargoTicketDetailId;

    @Column(name = "cargoTicketId", nullable = false)
    private int cargoTicketId;

    @Column(name = "cargoTypePriceId", nullable = false)
    private int cargoTypePriceId;

    @Column(name = "description")
    private String description;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "weightKg", nullable = false)
    private BigDecimal weightKg;

    @Column(name = "dimensionVol", nullable = false)
    private BigDecimal dimensionVol;

    @Column(name = "calculatedPrice", nullable = false)
    private BigDecimal calculatedPrice;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

   
}