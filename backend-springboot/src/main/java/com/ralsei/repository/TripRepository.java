package com.ralsei.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
import com.ralsei.dto.projection.cargoticket.CargoTicketTripOptionProjection;
import com.ralsei.dto.projection.trip.TripStopProjection;
import com.ralsei.dto.projection.trip.TripResourceProjection;
import com.ralsei.model.Trip;

import jakarta.transaction.Transactional;

public interface TripRepository extends JpaRepository<Trip, Integer> {

  @Query(value = """
      WITH eligible_trip AS (
          SELECT t.tripId, t.routeId, t.coachId, t.departureTime, t.[status],
                 MAX(pickup.minutesFromStart) AS pickupMinutes
          FROM trip t
          JOIN route_stop pickup ON pickup.routeId = t.routeId
          JOIN coach_stop pickupPoint ON pickupPoint.stopPointId = pickup.stopPointId
          JOIN coach_stop selectedPickup ON selectedPickup.stopPointId = :pickupStopId
          JOIN route_stop dropoff ON dropoff.routeId = t.routeId AND pickup.stopOrder < dropoff.stopOrder
          JOIN coach_stop dropoffPoint ON dropoffPoint.stopPointId = dropoff.stopPointId
          JOIN coach_stop selectedDropoff ON selectedDropoff.stopPointId = :dropoffStopId
          WHERE pickupPoint.city = selectedPickup.city
            AND dropoffPoint.city = selectedDropoff.city
            AND t.[status] IN ('SCHEDULED', 'IN_PROGRESS')
          GROUP BY t.tripId, t.routeId, t.coachId, t.departureTime, t.[status]
      )
      SELECT e.tripId AS tripId, r.routeName AS routeName,
             e.departureTime AS departureTime, c.licensePlate AS licensePlate,
             e.[status] AS tripStatus,
             DATEADD(MINUTE, e.pickupMinutes, e.departureTime) AS pickupTime,
             :pickupStopId AS pickupStopId, :dropoffStopId AS dropoffStopId
      FROM eligible_trip e
      JOIN route r ON r.routeId = e.routeId
      JOIN coach c ON c.coachId = e.coachId
      WHERE DATEADD(MINUTE, e.pickupMinutes, e.departureTime) >= GETDATE()
      ORDER BY DATEADD(MINUTE, e.pickupMinutes, e.departureTime)
      """, nativeQuery = true)
  java.util.List<CargoTicketTripOptionProjection> findCargoTicketTripOptions(
      @Param("pickupStopId") int pickupStopId,
      @Param("dropoffStopId") int dropoffStopId);

  @Query(value = """
      SELECT CASE WHEN EXISTS (
          SELECT 1
      FROM trip t
      JOIN route_stop pickup ON pickup.routeId = t.routeId
      JOIN coach_stop pickupPoint ON pickupPoint.stopPointId = pickup.stopPointId
      JOIN coach_stop selectedPickup ON selectedPickup.stopPointId = :pickupStopId
      JOIN route_stop dropoff ON dropoff.routeId = t.routeId AND pickup.stopOrder < dropoff.stopOrder
      JOIN coach_stop dropoffPoint ON dropoffPoint.stopPointId = dropoff.stopPointId
      JOIN coach_stop selectedDropoff ON selectedDropoff.stopPointId = :dropoffStopId
      WHERE t.tripId = :tripId
        AND pickupPoint.city = selectedPickup.city
        AND dropoffPoint.city = selectedDropoff.city
        AND DATEADD(MINUTE, pickup.minutesFromStart, t.departureTime) >= GETDATE()
        AND t.[status] IN ('SCHEDULED', 'IN_PROGRESS')
      ) THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END
      """, nativeQuery = true)
  boolean isEligibleForCargo(@Param("tripId") int tripId,
      @Param("pickupStopId") int pickupStopId,
      @Param("dropoffStopId") int dropoffStopId);

  /***
   * insertTrip: this method use to insert new trip from user
   * 
   * @param routeId       from FE
   * @param coachId       from FE
   * @param departureTime from FE
   * @param status        from FE
   * @param driverId      from FE
   * @param attendantId   from FEs
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
   * Atomically updates an open trip using only statuses accepted by
   * CK_Trip_Status.
   *
   * @return one when updated, otherwise zero
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      UPDATE trip
      SET routeId = :routeId, coachId = :coachId,
          departureTime = :departureTime, [status] = :status,
          driverId = :driverId, attendantId = :attendantId,
          updatedAt = GETDATE()
      WHERE tripId = :tripId
        AND [status] NOT IN ('CANCELLED', 'COMPLETED')
      """, nativeQuery = true)
  int updateOpenTrip(
      @Param("tripId") Integer tripId,
      @Param("routeId") Integer routeId,
      @Param("coachId") Integer coachId,
      @Param("departureTime") LocalDateTime departureTime,
      @Param("status") String status,
      @Param("driverId") Integer driverId,
      @Param("attendantId") Integer attendantId);

  /**
   * Soft-deletes an open trip as CANCELLED. Physical deletion is forbidden by
   * ticket and seat foreign keys and would destroy operational history.
   *
   * @return one when cancelled, otherwise zero
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      UPDATE trip
      SET [status] = 'CANCELLED', updatedAt = GETDATE()
      WHERE tripId = :tripId
        AND [status] NOT IN ('CANCELLED', 'COMPLETED')
      """, nativeQuery = true)
  int cancelOpenTrip(@Param("tripId") Integer tripId);

  /**
   * Searches customer trips with optional time, coach type, and price filters.
   * Seat availability is counted directly from the concrete trip_seat
   * snapshot for each trip. Price is resolved by the trip departure time so
   * old or future trips do not disappear because today's price row changed.
   * Trips whose departure time has already passed are excluded using the
   * request timestamp supplied by the service.
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
        AND t.departureTime >= :currentTime
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
        AND t.departureTime >= :currentTime
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
      @Param("currentTime") LocalDateTime currentTime,
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
   * the frontend does not need a separate placeholder mapping. Departed
   * trips are excluded using the same request-time rule as filtered search.
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
        AND t.departureTime >= :currentTime
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
        AND t.departureTime >= :currentTime
        AND r.routeName = :route
        AND t.[status] = 'SCHEDULED'
      """, nativeQuery = true)
  Page<TripDetailProjection> findTripDetails(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("currentTime") LocalDateTime currentTime,
      @Param("route") String route,
      Pageable pageable);

  /**
   * Returns the manager trip table for one day, optionally narrowed by route and
   * half-day. Route and crew identifiers are included so the edit form can be
   * populated without a second database-shaped details request.
   */
  @Query(value = """
      SELECT
          t.tripId AS tripId,
          r.routeId AS routeId,
          r.routeName AS routeName,
          c.coachId AS coachId,
          t.driverId AS driverId,
          driver.staffName AS driverName,
          driver.phone AS driverPhone,
          t.attendantId AS attendantId,
          attendant.staffName AS attendantName,
          attendant.phone AS attendantPhone,
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
      LEFT JOIN route r ON t.routeId = r.routeId
      LEFT JOIN coach c ON t.coachId = c.coachId
      LEFT JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
      LEFT JOIN staff driver ON driver.staffId = t.driverId
      LEFT JOIN staff attendant ON attendant.staffId = t.attendantId
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
        AND t.departureTime >= :currentTime
        AND t.[status] NOT IN ('CANCELED', 'CANCELLED')
        AND (:routeId IS NULL OR t.routeId = :routeId)
        AND (:period IS NULL
             OR (:period = 'MORNING' AND CAST(t.departureTime AS TIME) < '12:00:00')
             OR (:period = 'EVENING' AND CAST(t.departureTime AS TIME) >= '12:00:00'))
      ORDER BY t.departureTime ASC
      """, countQuery = """
      SELECT COUNT(*)
      FROM trip t
      WHERE CAST(t.departureTime AS DATE) = :departureDate
        AND t.departureTime >= :currentTime
        AND t.[status] NOT IN ('CANCELED', 'CANCELLED')
        AND (:routeId IS NULL OR t.routeId = :routeId)
        AND (:period IS NULL
             OR (:period = 'MORNING' AND CAST(t.departureTime AS TIME) < '12:00:00')
             OR (:period = 'EVENING' AND CAST(t.departureTime AS TIME) >= '12:00:00'))
      """, nativeQuery = true)
  Page<TripSummaryProjection> viewAllTripSummaries(
      @Param("departureDate") String departureDate,
      @Param("currentTime") LocalDateTime currentTime,
      @Param("routeId") Integer routeId,
      @Param("period") String period,
      Pageable pageable);

  /**
   * Finds active coaches assigned to the selected route which do not overlap
   * another non-cancelled trip in the standard 7 hour 12 minute trip window.
   */
  @Query(value = """
      SELECT c.coachId AS id, c.licensePlate AS displayName,
             CONCAT(c.manufacturer, ' - ', ct.coachTypeName) AS secondaryText
      FROM coach c
      JOIN coach_type ct ON ct.coachTypeId = c.coachTypeId
      WHERE c.[status] = 'ACTIVE'
        AND (c.routeId IS NULL OR c.routeId = :routeId)
        AND NOT EXISTS (
            SELECT 1 FROM trip busy
            WHERE busy.coachId = c.coachId
              AND busy.tripId <> COALESCE(:excludeTripId, -1)
              AND busy.[status] NOT IN ('CANCELED', 'CANCELLED')
              AND busy.departureTime < DATEADD(MINUTE, 432, :departureTime)
              AND DATEADD(MINUTE, 432, busy.departureTime) > :departureTime
        )
      ORDER BY c.licensePlate
      """, nativeQuery = true)
  List<TripResourceProjection> findAvailableCoaches(
      @Param("routeId") Integer routeId,
      @Param("departureTime") LocalDateTime departureTime,
      @Param("excludeTripId") Integer excludeTripId);

  /**
   * Finds active trip staff in the requested position with no overlapping trip.
   */
  @Query(value = """
      SELECT s.staffId AS id, s.staffName AS displayName,
             CONCAT(s.phone, ' - ', s.staffPosition) AS secondaryText
      FROM staff s
      WHERE s.isActive = 1 AND s.staffPosition = :position
        AND NOT EXISTS (
            SELECT 1 FROM trip busy
            WHERE (busy.driverId = s.staffId OR busy.attendantId = s.staffId)
              AND busy.tripId <> COALESCE(:excludeTripId, -1)
              AND busy.[status] NOT IN ('CANCELED', 'CANCELLED')
              AND busy.departureTime < DATEADD(MINUTE, 432, :departureTime)
              AND DATEADD(MINUTE, 432, busy.departureTime) > :departureTime
        )
      ORDER BY s.staffName
      """, nativeQuery = true)
  List<TripResourceProjection> findAvailableStaff(
      @Param("position") String position,
      @Param("departureTime") LocalDateTime departureTime,
      @Param("excludeTripId") Integer excludeTripId);

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

  /**
   * Finds the ordered stop timeline for one concrete trip. The trip id is the
   * source of truth because a coach may serve different routes or directions
   * on the same day. Each stop time is forecast from the trip departure and
   * the route-specific minutes-from-start value.
   *
   * @param tripId concrete scheduled trip identifier
   * @return active route stops in travel order
   */
  @Query(value = """
      SELECT
          t.tripId AS tripId,
          r.routeName AS routeName,
          cs.stopPointId AS stopPointId,
          cs.stopPointName AS stopPointName,
          cs.[address] AS [address],
          cs.city AS city,
          rs.stopOrder AS stopOrder,
          rs.minutesFromStart AS minutesFromStart,
          DATEADD(MINUTE, rs.minutesFromStart, t.departureTime) AS estimatedStopTime
      FROM trip t
      JOIN route r ON r.routeId = t.routeId
      JOIN route_stop rs ON rs.routeId = t.routeId
      JOIN coach_stop cs ON cs.stopPointId = rs.stopPointId
      WHERE t.tripId = :tripId
        AND cs.isActive = 1
      ORDER BY rs.stopOrder ASC
      """, nativeQuery = true)
  List<TripStopProjection> findTripStopsByTripId(@Param("tripId") Integer tripId);

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
