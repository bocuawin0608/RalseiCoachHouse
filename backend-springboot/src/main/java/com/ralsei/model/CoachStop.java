package com.tuanvm.model;

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
}