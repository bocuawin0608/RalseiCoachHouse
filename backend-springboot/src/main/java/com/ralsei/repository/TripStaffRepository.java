package com.ralsei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.tripstaff.AssignedTripProjection;
import com.ralsei.dto.projection.tripstaff.PassengerBoardingProjection;
import com.ralsei.model.Trip;

public interface TripStaffRepository extends JpaRepository<Trip, Integer> {

    @Query(value = """
            SELECT
                t.tripId AS tripId,
                r.routeName AS routeName,
                t.departureTime AS departureTime,
                c.licensePlate AS licensePlate,
                ct.coachTypeName AS coachTypeName,
                t.[status] AS tripStatus,
                CASE WHEN t.driverId = :staffId THEN 'DRIVER' ELSE 'ATTENDANT' END AS assignedRole,
                (SELECT COUNT(*)
                 FROM passenger_ticket pt
                 JOIN passenger_ticket_detail ptd ON pt.passengerTicketId = ptd.passengerTicketId
                 WHERE pt.tripId = t.tripId
                   AND ptd.[status] IN ('CONFIRMED', 'CHECKED_IN')) AS totalPassengers,
                (SELECT COUNT(*)
                 FROM passenger_ticket pt
                 JOIN passenger_ticket_detail ptd ON pt.passengerTicketId = ptd.passengerTicketId
                 WHERE pt.tripId = t.tripId
                   AND ptd.[status] = 'CHECKED_IN') AS checkedInCount
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
            WHERE (t.driverId = :staffId OR t.attendantId = :staffId)
              AND CAST(t.departureTime AS DATE) = :date
            ORDER BY t.departureTime ASC
            """, nativeQuery = true)
    List<AssignedTripProjection> findAssignedTripsByStaffAndDate(
            @Param("staffId") Integer staffId,
            @Param("date") String date);

    @Query(value = """
            SELECT
                ptd.ticketDetailId AS ticketDetailId,
                ptd.fullName AS fullName,
                ptd.phone AS phone,
                ptd.seatCodeSnapshot AS seatCodeSnapshot,
                pt.pickupStopName AS pickupStopName,
                pt.dropoffStopName AS dropoffStopName,
                ptd.[status] AS status,
                ac.fullname AS childFullname,
                ac.birthYear AS childBirthYear
            FROM passenger_ticket pt
            JOIN passenger_ticket_detail ptd ON pt.passengerTicketId = ptd.passengerTicketId
            LEFT JOIN accompanied_child ac ON ac.ticketDetailId = ptd.ticketDetailId
            WHERE pt.tripId = :tripId
              AND ptd.[status] IN ('CONFIRMED', 'CHECKED_IN')
            ORDER BY ptd.seatCodeSnapshot ASC
            """, nativeQuery = true)
    List<PassengerBoardingProjection> findPassengersForTrip(@Param("tripId") Integer tripId);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM trip t
                WHERE t.tripId = :tripId
                  AND (t.driverId = :staffId OR t.attendantId = :staffId)
            ) THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END
            """, nativeQuery = true)
    boolean isStaffAssignedToTrip(@Param("tripId") Integer tripId, @Param("staffId") Integer staffId);
}
