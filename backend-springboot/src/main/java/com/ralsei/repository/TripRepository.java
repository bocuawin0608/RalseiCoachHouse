package com.ralsei.repository;

import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.model.Trip;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

 
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
    

    //Manager site - view all trip summaries
    @Query(value = "SELECT " +
            "    t.tripId, " +
            "    r.routeName, " +
            "    c.licensePlate, " +
            "    ct.coachTypeName, " +
            "    ctp.seatPrice, " +
            "    (SELECT COUNT(*) FROM trip_seat ts WHERE ts.tripId = t.tripId AND ts.status = 'Available') AS availableSeats, " +
            "    (SELECT COUNT(*) FROM trip_seat ts WHERE ts.tripId = t.tripId) AS totalSeats, " +
            "    CAST(t.departureTime AS TIME) AS departureTime, " +
            "    CAST(t.departureTime AS DATE) AS departureDate, " +
            "    c.[status] AS coachStatus " +
            "FROM trip t " +
            "JOIN [route] r ON t.routeId = r.routeId " +
            "JOIN coach c ON t.coachId = c.coachId " +
            "JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId " +
            "LEFT JOIN coach_type_price ctp ON ctp.coachTypeId = ct.coachTypeId", 
            countQuery = "SELECT COUNT(*) FROM trip", 
            nativeQuery = true)
    Page<TripSummaryProjection> viewAllTripSummaries(Pageable pageable);

}