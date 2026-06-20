package com.ralsei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.model.TripSeat;

public interface TripSeatRepository extends JpaRepository<TripSeat, Integer> {
    @Query(value = """
        SELECT new com.ralsei.dto.response.passengerbooking.TripSeatResponse(
            ts.tripSeatId,
            s.seatCode,
            s.rowIndex,
            s.colIndex,
            s.floorIndex,
            ts.status
        )
        FROM TripSeat ts
        LEFT JOIN Seat s ON s.seatId = ts.seatId
        WHERE ts.tripId = :tripId
    """)
    public List<TripSeatResponse> getSeatMap(@Param("tripId") Integer tripId);
}
