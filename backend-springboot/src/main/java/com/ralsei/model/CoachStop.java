package com.ralsei.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    @Builder.Default
    @Column(name = "surcharge", nullable = false, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal surcharge = BigDecimal.ZERO;
}