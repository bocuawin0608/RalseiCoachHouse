package com.ralsei.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routeId", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stopPointId", nullable = false)
    private CoachStop coachStop;

    @Column(name = "stopOrder", nullable = false)
    private int stopOrder;

    @Column(name = "kilometersFromStart", nullable = false)
    private BigDecimal kilometersFromStart;

    @Column(name = "minutesFromStart", nullable = false)
    private int minutesFromStart;
}