package com.ralsei.model;

import java.math.BigDecimal;
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
@Table(name = "cargo_type_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoTypePrice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTypePriceId")
    private int cargoTypePriceId;

    @Column(name = "cargoTypeId", nullable = false)
    private int cargoTypeId;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "pricePerUnit", nullable = false)
    private BigDecimal pricePerUnit;

    @Column(name = "startEffectiveDate", nullable = false)
    private LocalDateTime startEffectiveDate;

    @Column(name = "endEffectiveDate", nullable = false)
    private LocalDateTime endEffectiveDate;

   
}