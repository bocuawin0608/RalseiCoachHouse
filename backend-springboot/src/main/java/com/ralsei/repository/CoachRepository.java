package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
>>>>>>> main

import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.model.Coach;
<<<<<<< HEAD
@Repository
public interface CoachRepository extends JpaRepository<Coach, Integer> {}
=======

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
            WHERE 
                (:#{#filter.licensePlate} IS NULL OR c.licensePlate LIKE CONCAT('%', :#{#filter.licensePlate}, '%'))
                AND (:#{#filter.coachTypeId} IS NULL OR ct.coachTypeId = :#{#filter.coachTypeId})
                AND (COALESCE(:#{#filter.statuses}, NULL) IS NULL OR c.status IN :#{#filter.statuses})
            GROUP BY c.coachId, c.licensePlate, ct.coachTypeName, c.manufacturer, c.year, c.status
            ORDER BY
                CASE c.status
                    WHEN 'active' THEN 1
                    WHEN 'maintenance' THEN 2
                    WHEN 'retired' THEN 3
                    ELSE 4
                END ASC,
                ct.coachTypeName ASC
        """)
    Page<CoachResponse> searchCoaches(
        @Param("filter") CoachFilterRequest filter,
        Pageable pageable 
    );
}
>>>>>>> main
