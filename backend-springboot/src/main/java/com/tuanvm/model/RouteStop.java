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
@Table(name = "route_stop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop extends BaseEntity {
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

   
}