package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CargoType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CargoTypeRepository extends JpaRepository<CargoType, Integer> {
    Page<CargoType> findByCargoTypeNameContainingIgnoreCase(String cargoTypeName, Pageable pageable);

    @Query("SELECT c FROM CargoType c WHERE " +
            "(:search IS NULL OR LOWER(c.cargoTypeName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<CargoType> filterCargoTypes(@Param("search") String search, @Param("isActive") Boolean isActive,
            Pageable pageable);

    boolean existsByCargoTypeNameIgnoreCase(String cargoTypeName);
}
