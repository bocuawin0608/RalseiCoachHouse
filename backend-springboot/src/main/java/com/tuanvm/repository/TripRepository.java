package com.tuanvm.repository;

import com.tuanvm.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.tuanvm.dto.projection.TripDetailProjection;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    @Query(value = """
            SELECT r.routeName AS routeName,
                   sl.seatLayoutName AS seatLayoutName,
                   t.[status] AS status,
                   t.departureTime AS departureTime
            FROM [trip] t
            JOIN route R ON t.routeId = R.routeId
            JOIN coach C ON t.coachId = C.coachId
            JOIN seat_layout SL ON C.seatLayoutId = SL.seatLayoutId
            WHERE t.departureTime BETWEEN :start AND :end
            AND R.routeName = :route
            """, nativeQuery = true)
    List<TripDetailProjection> layThongTinChuyenXeComplex(@Param("start") String start, @Param("end") String end,
            @Param("route") String route);
}