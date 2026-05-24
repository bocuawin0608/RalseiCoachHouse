package com.tuanvm.model;

import java.time.LocalDate;
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
@Table(name = "accompanied_child")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccompaniedChild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accompaniedChildId")
    private int accompaniedChildId;

    @Column(name = "ticketDetailId", nullable = false, unique = true)
    private int ticketDetailId;

    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

}