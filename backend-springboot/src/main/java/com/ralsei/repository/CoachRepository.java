package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.model.Coach;
import com.ralsei.model.enums.CoachStatus;

public interface CoachRepository extends JpaRepository<Coach, Integer> {
    
    @Query(value = """
            SELECT new com.ralsei.dto.response.coach.CoachResponse(
                c.coachId,
                c.licensePlate,
                ct.coachTypeName,
                CONCAT(c.manufacturer, ' - ', c.year),
                COUNT(s.seatId),
                c.status
            )
            FROM Coach c
            JOIN c.coachType ct
            LEFT JOIN c.seats s ON s.isActive = true
            LEFT JOIN c.route r
            WHERE 
                (:#{#filter.licensePlate == null || #filter.licensePlate.trim().isEmpty()} = true OR c.licensePlate LIKE CONCAT('%', :#{#filter.licensePlate}, '%'))
                AND (:#{#filter.coachTypeId == null} = true OR ct.coachTypeId = :#{#filter.coachTypeId})
                AND (:#{#filter.routeName == null || #filter.routeName.trim().isEmpty()} = true OR r.routeName LIKE CONCAT('%', :#{#filter.routeName}, '%'))
                AND (:#{#filter.statuses == null || #filter.statuses.isEmpty()} = true OR c.status IN :#{#filter.statuses})
            GROUP BY c.coachId, c.licensePlate, ct.coachTypeName, c.manufacturer, c.year, c.status
            ORDER BY
                CASE c.status
                    WHEN 'ACTIVE' THEN 1
                    WHEN 'MAINTENANCE' THEN 2
                    WHEN 'RETIRED' THEN 3
                    ELSE 4
                END ASC
        """)
    Page<CoachResponse> searchCoaches(
        @Param("filter") CoachFilterRequest filter,
        Pageable pageable 
    );
    
    boolean existsByCoachType_CoachTypeIdAndStatusNot(Integer coachTypeId, CoachStatus status);
    boolean existsByLicensePlateIgnoreCase(String licensePlate);
}