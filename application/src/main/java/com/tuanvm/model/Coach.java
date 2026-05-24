package com.tuanvm.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "coach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coach {
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

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

   
}