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
@Table(name = "coach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coachId")
    private int coachId;

    @Column(name = "seatLayoutId", nullable = false)
    private int seatLayoutId;

    @Column(name = "licensePlate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "year")
    private Integer year;

   
}