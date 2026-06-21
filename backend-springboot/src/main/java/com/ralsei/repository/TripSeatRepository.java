package com.ralsei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.model.TripSeat;
import com.ralsei.model.enums.TripSeatStatus;

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
        LEFT JOIN ts.seat s
        WHERE ts.trip.tripId = :tripId
    """)
    public List<TripSeatResponse> getSeatMap(@Param("tripId") Integer tripId);

    @Query(value = "SELECT ts.tripSeatId FROM TripSeat ts WHERE ts.trip.tripId = :tripId AND ts.status = :status")
    public List<Integer> findTripSeatIdsByTripIdAndStatus(@Param("tripId") Integer tripId, @Param("status") TripSeatStatus status);
}
