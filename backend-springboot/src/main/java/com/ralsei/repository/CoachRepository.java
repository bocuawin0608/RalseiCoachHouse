package com.ralsei.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.coach.CoachLicensePlateProjection;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.model.Coach;
import com.ralsei.model.enums.CoachStatus;

/**
 * Provides persistence access for coach data.
 */
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
                    END ASC,
                    c.coachId DESC
            """)
    Page<CoachResponse> searchCoaches(
            @Param("filter") CoachFilterRequest filter,
            Pageable pageable);

    @EntityGraph(attributePaths = { "route", "coachType" })
    Optional<Coach> findCoachWithRelationsByCoachId(Integer coachId);

    boolean existsByCoachType_CoachTypeIdAndStatusNot(Integer coachTypeId, CoachStatus status);

    boolean existsByLicensePlateIgnoreCase(String licensePlate);

    List<Coach> findByCoachType_CoachTypeIdAndStatusNot(Integer coachTypeId, CoachStatus status);

    long countByCoachType_CoachTypeIdAndStatusNot(Integer coachTypeId, CoachStatus status);

    @Query(value = """
                SELECT c.licensePlate, ct.coachTypeName
                FROM coach c
                JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
                JOIN trip t ON t.coachId = c.coachId
                WHERE CAST(t.departureTime AS DATE) = :date
            """, nativeQuery = true)
    List<CoachLicensePlateProjection> getCoachInfo(@Param("date") LocalDate date);
}