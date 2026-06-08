package com.ralsei.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ralsei.model.Coach;
@Repository
public interface CoachRepository extends JpaRepository<Coach, Integer> {
@Query(value = "SELECT \n" + //
        "    t.tripId, \n" + //
        "    r.routeName, \n" + //
        "    c.licensePlate, \n" + //
        "    ct.coachTypeName,\n" + //
        "    ctp.seatPrice,\n" + //
        "    (SELECT COUNT(*) \n" + //
        "     FROM trip_seat ts \n" + //
        "     WHERE ts.tripId = t.tripId AND ts.status = 'Available') AS availableSeats,\n" + //
        "     (SELECT COUNT(*) FROM trip_seat ts WHERE ts.tripId = t.tripId) AS totalSeats,\n" + //
        "     CAST(t.departureTime AS TIME) AS departureTime,\n" + //
        "     CAST(t.departureTime AS DATE) AS departureDate,\n" + //
        "     c.[status] as coachStatus\n" + //
        "FROM trip t \n" + //
        "JOIN [route] r ON t.routeId = r.routeId \n" + //
        "JOIN coach c ON t.coachId = c.coachId\n" + //
        "JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId\n" + //
        "LEFT JOIN coach_type_price ctp ON ctp.coachTypeId = ct.coachTypeId\n" + //
        "ORDER BY t.departureTime ASC, t.tripId ASC", nativeQuery = true)
    int countByCoachTypeId(Integer coachTypeId);
}
