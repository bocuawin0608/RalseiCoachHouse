package com.ralsei.repository;

import com.ralsei.dto.projection.TripDetailProjection;
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
            SELECT t.tripId,sl.seatLayoutName,r.routeName, t.departureTime, slp.seatPrice
            FROM trip t
            JOIN route r ON t.routeId = r.routeId
            JOIN coach c ON t.coachId = c.coachId
            JOIN seat_layout sl ON c.seatLayoutId = sl.seatLayoutId
            JOIN seat_layout_price slp ON sl.seatLayoutId = slp.seatLayoutId
                      WHERE t.departureTime BETWEEN :start AND :end
                          AND r.routeName = :route
                  """, countQuery = """
                SELECT count(t.tripId)
                FROM trip t
                JOIN route r ON t.routeId = r.routeId
                WHERE t.departureTime BETWEEN :start AND :end
                    AND r.routeName = :route
            """, nativeQuery = true)
    Page<TripDetailProjection> findTripDetails(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("route") String route,
            Pageable pageable);
}