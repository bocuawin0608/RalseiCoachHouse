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
@Query("""
    SELECT 
        r.routeName AS routeName, 
        sl.seatLayoutName AS seatLayoutName, 
        t.status AS status, 
        t.departureTime AS departureTime
    FROM Trip t 
    JOIN t.route r
    JOIN t.coach c
    JOIN c.seatLayout sl
    WHERE t.departureTime BETWEEN :start AND :end
    AND r.routeName = :route
    ORDER BY t.departureTime
    """)
Page<TripDetailProjection> findTripDetails(
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end,
    @Param("route") String route,
    Pageable pageable
);
}