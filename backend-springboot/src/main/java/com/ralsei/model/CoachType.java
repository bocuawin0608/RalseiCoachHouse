package com.ralsei.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coach_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coachTypeId")
    private int coachTypeId;

    @Column(name = "coachTypeName", nullable = false, unique = true)
    private String coachTypeName;

    @Column(name = "totalSeat", nullable = false)
    private int totalSeat;

    @Column(name = "seatLayout", nullable = false, columnDefinition = "TEXT")
    private String seatLayout;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @Builder.Default
    @OneToMany(mappedBy="coachType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CoachTypePrice> coachTypePrices = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy="coachType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Coach> coaches = new ArrayList<>();
}
