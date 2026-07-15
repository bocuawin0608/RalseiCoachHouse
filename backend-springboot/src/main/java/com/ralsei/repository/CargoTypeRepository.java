package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CargoType;
import com.ralsei.dto.projection.cargotype.CargoTypeManagementProjection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Provides persistence access for cargo type data.
 */
public interface CargoTypeRepository extends JpaRepository<CargoType, Integer> {
    Page<CargoType> findByCargoTypeNameContainingIgnoreCase(String cargoTypeName, Pageable pageable);

    @Query("SELECT c FROM CargoType c WHERE " +
            "(:search IS NULL OR LOWER(c.cargoTypeName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<CargoType> filterCargoTypes(@Param("search") String search, @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Reads the staff cargo type management table with its current surcharge row.
     *
     * <p>The correlated subquery picks one latest price row per cargo type so
     * the screen has exactly one editable surcharge record for each type.</p>
     */
    @Query("""
            SELECT c.cargoTypeId AS cargoTypeId,
                   c.cargoTypeName AS cargoTypeName,
                   c.isActive AS active,
                   price.cargoTypePriceId AS cargoTypePriceId,
                   price.unit AS unit,
                   price.pricePerUnit AS pricePerUnit
            FROM CargoType c
            LEFT JOIN CargoTypePrice price
                   ON price.cargoTypeId = c.cargoTypeId
                  AND price.cargoTypePriceId = (
                      SELECT MAX(p2.cargoTypePriceId)
                      FROM CargoTypePrice p2
                      WHERE p2.cargoTypeId = c.cargoTypeId
                        AND p2.startEffectiveDate = (
                            SELECT MAX(p3.startEffectiveDate)
                            FROM CargoTypePrice p3
                            WHERE p3.cargoTypeId = c.cargoTypeId
                        )
                  )
            WHERE (:search IS NULL OR LOWER(c.cargoTypeName) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:isActive IS NULL OR c.isActive = :isActive)
            """)
    Page<CargoTypeManagementProjection> filterCargoTypeManagementRows(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    boolean existsByCargoTypeNameIgnoreCase(String cargoTypeName);
}
