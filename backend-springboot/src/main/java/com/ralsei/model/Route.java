package com.tuanvm.model;

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
@Table(name = "route")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routeId")
    private int routeId;

    @Column(name = "routeName", nullable = false)
    private String routeName;

    @Column(name = "totalKilometers", nullable = false)
    private BigDecimal totalKilometers;

    @Column(name = "totalMinutes", nullable = false)
    private int totalMinutes;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;
}