package com.ralsei.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coach_stop", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"address", "city"})
})
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

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "coachStop", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    @Builder.Default
    private Set<RouteStop> routeStops = new HashSet<>();
}