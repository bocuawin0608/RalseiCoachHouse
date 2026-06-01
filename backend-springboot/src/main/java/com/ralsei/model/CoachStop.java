package com.ralsei.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coach_stop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachStop extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stopPointId")
    private int stopPointId;

    @Column(name = "stopPointName", nullable = false)
    private String stopPointName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "coachStop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RouteStop> routeStops = new ArrayList<>();
}