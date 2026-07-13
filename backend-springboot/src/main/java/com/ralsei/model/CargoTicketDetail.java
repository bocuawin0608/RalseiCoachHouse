package com.ralsei.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cargo_ticket_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoTicketDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTicketDetailId")
    private int cargoTicketDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargoTicketId")
    private CargoTicket cargoTicket;

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

   
}