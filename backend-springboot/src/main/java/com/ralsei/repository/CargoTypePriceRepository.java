package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CargoTypePrice;

public interface CargoTypePriceRepository extends JpaRepository<CargoTypePrice, Integer> {
    Page<CargoTypePrice> findByCargoTypeId(int cargoTypeId, Pageable pageable);

    // Example custom query method if you need to search by unit as well
    Page<CargoTypePrice> findByUnitContainingIgnoreCase(String unit, Pageable pageable);

    java.util.List<CargoTypePrice> findByCargoTypeId(int cargoTypeId);
}
