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
            SELECT t.route.routeName AS routeName,
                   t.coach.seatLayout.seatLayoutName AS seatLayoutName,
                   t.status AS status,
                   t.departureTime AS departureTime
            FROM Trip t
            WHERE t.departureTime BETWEEN :start AND :end
              AND t.route.routeName = :route
            """,
            countQuery = """
            SELECT COUNT(t) 
            FROM Trip t 
            WHERE t.departureTime BETWEEN :start AND :end
              AND t.route.routeName = :route
            """) // Câu lệnh count gọn nhẹ, không JOIN thừa thãi
    Page<TripDetailProjection> findTripDetails(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end,
            @Param("route") String route,
            Pageable pageable
    );
}