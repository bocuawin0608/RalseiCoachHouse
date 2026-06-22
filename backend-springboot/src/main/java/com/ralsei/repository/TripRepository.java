package com.ralsei.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.model.Trip;

import jakarta.transaction.Transactional;

public interface TripRepository extends JpaRepository<Trip, Integer> {
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
            VALUES (:routeId, :coachId, :departureTime, :status, :driverId, :attendantId)
            """, nativeQuery = true)
    void insertTrip(
            @Param("routeId") Integer routeId,
            @Param("coachId") Integer coachId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("status") String status,
            @Param("driverId") Integer driverId,
            @Param("attendantId") Integer attendantId);

    @Query(value = """
            SELECT
                t.tripId AS tripId,
                ct.coachTypeName AS coachTypeName,
                r.routeName AS routeName,
                t.departureTime AS departureTime,
                ctp.seatPrice AS seatPrice
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
            WHERE r.routeName = :route
              AND t.departureTime BETWEEN :start AND :end
              AND :now BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
              AND (:checkTimeSlots = 0 OR (
                  (:slot1Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot1Start AND :slot1End) OR
                  (:slot2Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot2Start AND :slot2End) OR
                  (:slot3Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot3Start AND :slot3End) OR
                  (:slot4Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot4Start AND :slot4End)
              ))
              AND (:checkLayouts = 0 OR (
                  ct.coachTypeName LIKE :layoutKeyword1 OR
                  ct.coachTypeName LIKE :layoutKeyword2
              ))
              AND (:minPrice IS NULL OR ctp.seatPrice >= :minPrice)
              AND (:maxPrice IS NULL OR ctp.seatPrice <= :maxPrice)
            """, countQuery = """
            SELECT COUNT(t.tripId)
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
            WHERE r.routeName = :route
              AND t.departureTime BETWEEN :start AND :end
              AND :now BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
              AND (:checkTimeSlots = 0 OR (
                  (:slot1Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot1Start AND :slot1End) OR
                  (:slot2Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot2Start AND :slot2End) OR
                  (:slot3Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot3Start AND :slot3End) OR
                  (:slot4Start IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN :slot4Start AND :slot4End)
              ))
              AND (:checkLayouts = 0 OR (
                  ct.coachTypeName LIKE :layoutKeyword1 OR
                  ct.coachTypeName LIKE :layoutKeyword2
              ))
              AND (:minPrice IS NULL OR ctp.seatPrice >= :minPrice)
              AND (:maxPrice IS NULL OR ctp.seatPrice <= :maxPrice)
            """, nativeQuery = true)
    Page<TripFilterProjection> filterTrips(
            @Param("now") LocalDateTime now,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("route") String route,
            @Param("checkTimeSlots") int checkTimeSlots,
            @Param("slot1Start") String slot1Start, @Param("slot1End") String slot1End,
            @Param("slot2Start") String slot2Start, @Param("slot2End") String slot2End,
            @Param("slot3Start") String slot3Start, @Param("slot3End") String slot3End,
            @Param("slot4Start") String slot4Start, @Param("slot4End") String slot4End,
            @Param("checkLayouts") int checkLayouts,
            @Param("layoutKeyword1") String layoutKeyword1,
            @Param("layoutKeyword2") String layoutKeyword2,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query(value = """
            SELECT t.tripId, ct.coachTypeName, r.routeName, t.departureTime, ctp.seatPrice
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
            WHERE t.departureTime BETWEEN :start AND :end
              AND r.routeName = :route
            ORDER BY t.departureTime
            """, countQuery = """
            SELECT COUNT(t.tripId)
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
            WHERE t.departureTime BETWEEN :start AND :end
              AND r.routeName = :route
            """, nativeQuery = true)
    Page<TripDetailProjection> findTripDetails(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("route") String route,
            Pageable pageable);

    // Manager site - view all trip summaries
    @Query(value = """
            SELECT
                t.tripId AS tripId,
                t.[status] AS tripStatus,
                c.manufacturer AS manufacturer,
                ct.coachTypeName AS coachTypeName,
                c.licensePlate AS licensePlate,
                c.[status] AS coachStatus,
                CAST(t.departureTime AS DATE) AS departureDate,
                CAST(t.departureTime AS TIME) AS departureTime,
                (SELECT COUNT(*) FROM trip_seat ts WHERE ts.tripId = t.tripId AND ts.[status] = 'AVAILABLE') AS availableSeats,
                (SELECT COUNT(*) FROM trip_seat ts WHERE ts.tripId = t.tripId) AS totalSeats
            FROM trip t
            LEFT JOIN coach c ON t.coachId = c.coachId
            LEFT JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            WHERE CAST(t.departureTime AS DATE) = :departureDate
            ORDER BY t.departureTime ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM trip t
            WHERE CAST(t.departureTime AS DATE) = :departureDate
            """, nativeQuery = true)
    Page<TripSummaryProjection> viewAllTripSummaries(
            @Param("departureDate") String departureDate,
            Pageable pageable);

    @Query(value = "SELECT MAX(CAST(departureTime AS DATE)) FROM [trip]", nativeQuery = true)
    LocalDate findMaxDepartureDate();

    @Transactional
    @Procedure(name = "sp_AutoGenerateWeeklySchedule_Final")
    public void autoGenerateWeeklySchedule(@Param("StartDate") String startDate);

    @Query(value = """
                SELECT CASE WHEN EXISTS (
                    SELECT 1 FROM coach c WHERE c.coachId = :coachId AND EXISTS (
                        SELECT 1 FROM trip t WHERE t.coachId = c.coachId
                            AND departureTime >= DATEADD(hour, -8, GETDATE())
                            AND t.status NOT IN ('CANCELLED', 'COMPLETED')
                    )
                ) THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END
            """, nativeQuery = true)
    boolean checkIfCoachHasTodoTrips(@Param("coachId") Integer coachId);

    boolean existsByCoach_CoachId(Integer coachId);

    @Query(value = """
            SELECT COUNT(DISTINCT CAST(departureTime AS DATE))
            FROM trip
            WHERE departureTime >= :startOfDay
              AND departureTime <= :endOfPeriod
            """, nativeQuery = true)
    int countDistinctDaysWithSchedule(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfPeriod") LocalDateTime endOfPeriod);
}