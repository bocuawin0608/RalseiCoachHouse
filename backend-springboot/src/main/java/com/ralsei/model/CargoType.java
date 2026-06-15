package com.ralsei.model;

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
@Table(name = "cargo_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargoTypeId")
    private int cargoTypeId;

    @Column(name = "cargoTypeName", nullable = false, unique = true)
    private String cargoTypeName;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;

   
}