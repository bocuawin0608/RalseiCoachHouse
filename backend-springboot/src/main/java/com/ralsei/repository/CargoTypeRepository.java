package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ralsei.model.CargoType;

@Repository
public interface CargoTypeRepository extends JpaRepository<CargoType, Integer> {
    Page<CargoType> findByCargoTypeNameContainingIgnoreCase(String cargoTypeName, Pageable pageable);
    
    boolean existsByCargoTypeNameIgnoreCase(String cargoTypeName);
}
