package com.ralsei.staff.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides the trip component for the application.
 */
@Entity
@Table(name = "trip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Trip extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tripId")
    private int tripId;

    @Column(name = "routeId", nullable = false)
    private int routeId;

    @Column(name = "coachId", nullable = false)
    private int coachId;

    @Column(name = "departureTime", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "driverId", nullable = false)
    private int driverId;

    @Column(name = "attendantId", nullable = false)
    private int attendantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routeId", insertable = false, updatable = false)
    private Route route;

    // [MICROSERVICE-REFACTOR]: Removed @ManyToOne Coach. Use scalar coachId + FeignClient.
    // coachId is retained as int scalar field.

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CargoTicket> cargoTickets;

    @jakarta.persistence.Transient
    private Coach coach;
    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }
}