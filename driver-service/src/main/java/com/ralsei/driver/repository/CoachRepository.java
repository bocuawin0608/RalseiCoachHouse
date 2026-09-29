package com.ralsei.driver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.driver.dto.request.coach.CoachFilterRequest;
import com.ralsei.driver.dto.response.coach.CoachResponse;
import com.ralsei.driver.model.Coach;
import com.ralsei.driver.model.CoachStatus;

import jakarta.persistence.LockModeType;

public interface CoachRepository extends JpaRepository<Coach, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coach c WHERE c.coachId = :coachId")
    Optional<Coach> findByIdForUpdate(@Param("coachId") Integer coachId);

    // [MICROSERVICE-REFACTOR]: Removed 'route' JOIN and 'routeName' SQL filter.
    @Query(value = """
                SELECT new com.ralsei.driver.dto.response.coach.CoachResponse(
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
                    (:#{#filter.licensePlate == null || #filter.licensePlate.trim().isEmpty()} = true OR c.licensePlate LIKE CONCAT('%', :#{#filter.licensePlate}, '%'))
                    AND (:#{#filter.coachTypeId == null} = true OR ct.coachTypeId = :#{#filter.coachTypeId})
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

    @EntityGraph(attributePaths = { "coachType" })
    Optional<Coach> findCoachWithRelationsByCoachId(Integer coachId);

    boolean existsByCoachType_CoachTypeIdAndStatusNot(Integer coachTypeId, CoachStatus status);

    boolean existsByLicensePlateIgnoreCase(String licensePlate);

    List<Coach> findByCoachType_CoachTypeIdAndStatusNot(Integer coachTypeId, CoachStatus status);

    long countByCoachType_CoachTypeIdAndStatusNot(Integer coachTypeId, CoachStatus status);
}
