package com.tuanvm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "route_stop")
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

    public RouteStop() {
    }

    public RouteStop(int routeStopId, int routeId, int stopPointId, int stopOrder, BigDecimal kilometersFromStart,
            int minutesFromStart, LocalDateTime createdAt, Integer createdBy, LocalDateTime updatedAt,
            Integer updatedBy) {
        this.routeStopId = routeStopId;
        this.routeId = routeId;
        this.stopPointId = stopPointId;
        this.stopOrder = stopOrder;
        this.kilometersFromStart = kilometersFromStart;
        this.minutesFromStart = minutesFromStart;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public int getRouteStopId() {
        return routeStopId;
    }

    public void setRouteStopId(int routeStopId) {
        this.routeStopId = routeStopId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getStopPointId() {
        return stopPointId;
    }

    public void setStopPointId(int stopPointId) {
        this.stopPointId = stopPointId;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(int stopOrder) {
        this.stopOrder = stopOrder;
    }

    public BigDecimal getKilometersFromStart() {
        return kilometersFromStart;
    }

    public void setKilometersFromStart(BigDecimal kilometersFromStart) {
        this.kilometersFromStart = kilometersFromStart;
    }

    public int getMinutesFromStart() {
        return minutesFromStart;
    }

    public void setMinutesFromStart(int minutesFromStart) {
        this.minutesFromStart = minutesFromStart;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }
}