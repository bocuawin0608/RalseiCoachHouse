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
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staffId")
    private int staffId;

    @Column(name = "accountId", unique = true)
    private Integer accountId;

    @Column(name = "ticketAgencyId")
    private Integer ticketAgencyId;

    @Column(name = "staffName", nullable = false)
    private String staffName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "staffPosition", nullable = false)
    private String staffPosition;

    @Column(name = "hireDate", nullable = false)
    private LocalDate hireDate;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "updatedBy")
    private Integer updatedBy;

  
}