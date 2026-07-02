package com.ralsei.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

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
    
    /***
     * insertTrip: this method use to insert new trip from user
     * @param routeId from FE
     * @param coachId from FE
     * @param departureTime from FE
     * @param status from FE
     * @param driverId from FE
     * @param attendantId from FEs
     */
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

    /**
     * Searches customer trips with optional time, coach type, and price filters.
     * Seat availability is counted directly from the concrete trip_seat
     * snapshot for each trip. Price is resolved by the trip departure time so
     * old or future trips do not disappear because today's price row changed.
     */
    @Query(value = """
            SELECT
                t.tripId AS tripId,
                ct.coachTypeName AS coachTypeName,
                r.routeName AS routeName,
                t.departureTime AS departureTime,
                DATEADD(MINUTE, 432, t.departureTime) AS arrivalTime,
                N'7 giờ 12 phút' AS duration,
                ctp.seatPrice AS seatPrice,
                COALESCE(seat_counts.availableSeats, 0) AS availableSeats,
                COALESCE(seat_counts.totalSeats, 0) AS totalSeats
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
                AND t.departureTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
            LEFT JOIN (
                SELECT
                    ts.tripId,
                    CAST(SUM(CASE WHEN UPPER(LTRIM(RTRIM(ts.[status]))) = 'AVAILABLE' THEN 1 ELSE 0 END) AS INT) AS availableSeats,
                    CAST(COUNT(ts.tripSeatId) AS INT) AS totalSeats
                FROM trip_seat ts
                JOIN seat s ON s.seatId = ts.seatId
                WHERE s.isActive = 1
                GROUP BY ts.tripId
            ) seat_counts ON seat_counts.tripId = t.tripId
            WHERE r.routeName = :route
              AND t.departureTime BETWEEN :start AND :end
              AND t.[status] = 'SCHEDULED'
              AND (:checkTimeSlots = 0 OR (
                  (:slot1StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot1StartMinute AND :slot1EndMinute) OR
                  (:slot2StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot2StartMinute AND :slot2EndMinute) OR
                  (:slot3StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot3StartMinute AND :slot3EndMinute) OR
                  (:slot4StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot4StartMinute AND :slot4EndMinute)
              ))
              AND (:checkLayouts = 0 OR (
                  LOWER(ct.coachTypeName) LIKE LOWER(:layoutKeyword1) OR
                  LOWER(ct.coachTypeName) LIKE LOWER(:layoutKeyword2) OR
                  LOWER(ct.coachTypeName) LIKE LOWER(:layoutKeyword3)
              ))
              AND (:minPrice IS NULL OR ctp.seatPrice >= :minPrice)
              AND (:maxPrice IS NULL OR ctp.seatPrice <= :maxPrice)
            ORDER BY t.departureTime
            """, countQuery = """
            SELECT COUNT(t.tripId)
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
                AND t.departureTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
            WHERE r.routeName = :route
              AND t.departureTime BETWEEN :start AND :end
              AND t.[status] = 'SCHEDULED'
              AND (:checkTimeSlots = 0 OR (
                  (:slot1StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot1StartMinute AND :slot1EndMinute) OR
                  (:slot2StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot2StartMinute AND :slot2EndMinute) OR
                  (:slot3StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot3StartMinute AND :slot3EndMinute) OR
                  (:slot4StartMinute IS NOT NULL AND DATEDIFF(MINUTE, CAST(t.departureTime AS DATE), t.departureTime) BETWEEN :slot4StartMinute AND :slot4EndMinute)
              ))
              AND (:checkLayouts = 0 OR (
                  LOWER(ct.coachTypeName) LIKE LOWER(:layoutKeyword1) OR
                  LOWER(ct.coachTypeName) LIKE LOWER(:layoutKeyword2) OR
                  LOWER(ct.coachTypeName) LIKE LOWER(:layoutKeyword3)
              ))
              AND (:minPrice IS NULL OR ctp.seatPrice >= :minPrice)
              AND (:maxPrice IS NULL OR ctp.seatPrice <= :maxPrice)
            """, nativeQuery = true)
    Page<TripFilterProjection> filterTrips(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("route") String route,
            @Param("checkTimeSlots") int checkTimeSlots,
            @Param("slot1StartMinute") Integer slot1StartMinute, @Param("slot1EndMinute") Integer slot1EndMinute,
            @Param("slot2StartMinute") Integer slot2StartMinute, @Param("slot2EndMinute") Integer slot2EndMinute,
            @Param("slot3StartMinute") Integer slot3StartMinute, @Param("slot3EndMinute") Integer slot3EndMinute,
            @Param("slot4StartMinute") Integer slot4StartMinute, @Param("slot4EndMinute") Integer slot4EndMinute,
            @Param("checkLayouts") int checkLayouts,
            @Param("layoutKeyword1") String layoutKeyword1,
            @Param("layoutKeyword2") String layoutKeyword2,
            @Param("layoutKeyword3") String layoutKeyword3,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    /**
     * Loads the default customer trip list for a route/date before any filters
     * are selected. The projection shape intentionally matches filterTrips so
     * the frontend does not need a separate placeholder mapping.
     */
    @Query(value = """
            SELECT
                t.tripId AS tripId,
                ct.coachTypeName AS coachTypeName,
                r.routeName AS routeName,
                ct.seatLayout AS seatLayoutName,
                t.[status] AS status,
                t.departureTime AS departureTime,
                DATEADD(MINUTE, 432, t.departureTime) AS arrivalTime,
                N'7 giờ 12 phút' AS duration,
                ctp.seatPrice AS seatPrice,
                COALESCE(seat_counts.availableSeats, 0) AS availableSeats,
                COALESCE(seat_counts.totalSeats, 0) AS totalSeats
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
                AND t.departureTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
            LEFT JOIN (
                SELECT
                    ts.tripId,
                    CAST(SUM(CASE WHEN UPPER(LTRIM(RTRIM(ts.[status]))) = 'AVAILABLE' THEN 1 ELSE 0 END) AS INT) AS availableSeats,
                    CAST(COUNT(ts.tripSeatId) AS INT) AS totalSeats
                FROM trip_seat ts
                JOIN seat s ON s.seatId = ts.seatId
                WHERE s.isActive = 1
                GROUP BY ts.tripId
            ) seat_counts ON seat_counts.tripId = t.tripId
            WHERE t.departureTime BETWEEN :start AND :end
              AND r.routeName = :route
              AND t.[status] = 'SCHEDULED'
            ORDER BY t.departureTime
            """, countQuery = """
            SELECT COUNT(t.tripId)
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
                AND t.departureTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
            WHERE t.departureTime BETWEEN :start AND :end
              AND r.routeName = :route
              AND t.[status] = 'SCHEDULED'
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
                COALESCE(seat_counts.availableSeats, 0) AS availableSeats,
                COALESCE(seat_counts.totalSeats, 0) AS totalSeats
            FROM trip t
            LEFT JOIN coach c ON t.coachId = c.coachId
            LEFT JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            LEFT JOIN (
                SELECT
                    ts.tripId,
                    CAST(SUM(CASE WHEN UPPER(LTRIM(RTRIM(ts.[status]))) = 'AVAILABLE' THEN 1 ELSE 0 END) AS INT) AS availableSeats,
                    CAST(COUNT(ts.tripSeatId) AS INT) AS totalSeats
                FROM trip_seat ts
                JOIN seat s ON s.seatId = ts.seatId
                WHERE s.isActive = 1
                GROUP BY ts.tripId
            ) seat_counts ON seat_counts.tripId = t.tripId
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

    /**
     * Counts available seats for one concrete trip from its trip_seat snapshot.
     * The tripId already owns the coach/date context, so no external route,
     * coach, or date parameter is needed.
     */
    @Query(value = """
            SELECT CAST(COUNT(1) AS INT)
            FROM trip_seat ts
            JOIN seat s ON s.seatId = ts.seatId
            WHERE ts.tripId = :tripId
              AND s.isActive = 1
              AND UPPER(LTRIM(RTRIM(ts.[status]))) = 'AVAILABLE'
            """, nativeQuery = true)
    Integer countAvailableTripSeatsByTripId(@Param("tripId") Integer tripId);

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

    @Query(value = """
                SELECT COUNT(*)
                FROM trip t
                WHERE t.coachId = :coachId
                    AND t.departureTime >= DATEADD(hour, -8, GETDATE())
                    AND t.status NOT IN ('CANCELLED', 'COMPLETED')
            """, nativeQuery = true)
    long countUpcomingTripsByCoachId(@Param("coachId") Integer coachId);

    boolean existsByCoach_CoachId(Integer coachId);

    @Query(value = """
            SELECT COUNT(DISTINCT CAST(departureTime AS DATE))
            FROM trip
            WHERE departureTime >= :startOfDay
              AND departureTime <= :endOfPeriod
            """, nativeQuery = true)
    int countDistinctDaysWithSchedule(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfPeriod") LocalDateTime endOfPeriod);

    @Query("""
            SELECT t FROM Trip t
            JOIN FETCH t.route
            JOIN FETCH t.coach c
            JOIN FETCH c.coachType
            WHERE t.tripId = :tripId
            """)
    Optional<Trip> findByIdWithRouteAndCoach(@Param("tripId") Integer tripId);
}
