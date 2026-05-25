package com.tuanvm.model;

import java.time.LocalDate;

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
@Table(name = "accompanied_child")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccompaniedChild extends BaseEntity {
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

}