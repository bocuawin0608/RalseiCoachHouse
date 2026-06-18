package com.ralsei.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.CoachTypeProjection;
import com.ralsei.dto.request.coachtype.CoachTypeFilterRequest;
import com.ralsei.dto.response.coachtype.CoachTypeDropdownDTO;
import com.ralsei.model.CoachType;

public interface CoachTypeRepository extends JpaRepository<CoachType, Integer> {
    @Query(value = """
        SELECT 
            sl.coachTypeId, sl.coachTypeName, sl.totalSeat, sl.isActive,
            p.seatPrice AS currentPrice,
            (SELECT COUNT(1) FROM coach c WHERE c.coachTypeId = sl.coachTypeId) AS totalCoach
        FROM coach_type sl
        LEFT JOIN coach_type_price p 
            ON p.coachTypeId = sl.coachTypeId AND :now BETWEEN p.startEffectiveDate AND p.endEffectiveDate
        WHERE (:#{#filter.coachTypeName} IS NULL OR sl.coachTypeName LIKE '%' + :#{#filter.coachTypeName} + '%')
            AND (:#{#filter.isActive} IS NULL OR sl.isActive = :#{#filter.isActive})
            AND (:#{#filter.minPrice} IS NULL OR p.seatPrice >= :#{#filter.minPrice})
            AND (:#{#filter.maxPrice} IS NULL OR p.seatPrice <= :#{#filter.maxPrice})
            AND (:#{#filter.minSeats} IS NULL OR sl.totalSeat >= :#{#filter.minSeats})
            AND (:#{#filter.maxSeats} IS NULL OR sl.totalSeat <= :#{#filter.maxSeats})
        """, 
        nativeQuery = true,
        countQuery = """
            SELECT COUNT(DISTINCT sl.coachTypeId)
            FROM coach_type sl
            LEFT JOIN coach_type_price p 
                ON p.coachTypeId = sl.coachTypeId AND :now BETWEEN p.startEffectiveDate AND p.endEffectiveDate
            WHERE (:#{#filter.coachTypeName} IS NULL OR sl.coachTypeName LIKE '%' + :#{#filter.coachTypeName} + '%')
                AND (:#{#filter.isActive} IS NULL OR sl.isActive = :#{#filter.isActive})
                AND (:#{#filter.minPrice} IS NULL OR p.seatPrice >= :#{#filter.minPrice})
                AND (:#{#filter.maxPrice} IS NULL OR p.seatPrice <= :#{#filter.maxPrice})
                AND (:#{#filter.minSeats} IS NULL OR sl.totalSeat >= :#{#filter.minSeats})
                AND (:#{#filter.maxSeats} IS NULL OR sl.totalSeat <= :#{#filter.maxSeats})
        """)
    Page<CoachTypeProjection> searchCoachTypes(
        @Param("filter") CoachTypeFilterRequest filter,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    boolean existsByCoachTypeNameIgnoreCase(String coachTypeName);
    Optional<CoachType> findByCoachTypeIdAndIsActiveTrue(Integer id);
    
    @Query("SELECT new com.ralsei.dto.response.coachtype.CoachTypeDropdownDTO(ct.coachTypeId, ct.coachTypeName) FROM CoachType ct WHERE ct.isActive = true ORDER BY ct.coachTypeName")
    List<CoachTypeDropdownDTO> findActiveCoachTypesForDropdown();
}
