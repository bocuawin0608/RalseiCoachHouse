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
@Table(name = "route_stop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routeStopId")
    private int routeStopId;

    @Column(name = "routeId", nullable = false)
    private int routeId;

    @Column(name = "stopPointId", nullable = false)
    private int stopPointId;

    @Column(name = "stopOrder", nullable = false)
    private int stopOrder;

    @Column(name = "kilometersFromStart", nullable = false)
    private BigDecimal kilometersFromStart;

    @Column(name = "minutesFromStart", nullable = false)
    private int minutesFromStart;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

   
}