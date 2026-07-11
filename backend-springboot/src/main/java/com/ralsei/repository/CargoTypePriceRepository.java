package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.CargoTypePrice;

@Repository
public interface CargoTypePriceRepository extends JpaRepository<CargoTypePrice, Integer> {
    Page<CargoTypePrice> findByCargoTypeId(int cargoTypeId, Pageable pageable);

    // Example custom query method if you need to search by unit as well
    Page<CargoTypePrice> findByUnitContainingIgnoreCase(String unit, Pageable pageable);

    java.util.List<CargoTypePrice> findByCargoTypeId(int cargoTypeId);

    /**
     * Finds the latest surcharge row for a cargo type.
     *
     * <p>The staff management screen edits one surcharge record per cargo type;
     * using latest effective date and then highest id makes that record
     * deterministic.</p>
     */
    @Query("""
            SELECT price
            FROM CargoTypePrice price
            WHERE price.cargoTypeId = :cargoTypeId
              AND price.cargoTypePriceId = (
                  SELECT MAX(p2.cargoTypePriceId)
                  FROM CargoTypePrice p2
                  WHERE p2.cargoTypeId = :cargoTypeId
                    AND p2.startEffectiveDate = (
                        SELECT MAX(p3.startEffectiveDate)
                        FROM CargoTypePrice p3
                        WHERE p3.cargoTypeId = :cargoTypeId
                    )
              )
            """)
    java.util.Optional<CargoTypePrice> findLatestByCargoTypeId(@Param("cargoTypeId") int cargoTypeId);
}
