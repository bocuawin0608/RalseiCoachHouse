package com.ralsei.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("""
        SELECT ts
        FROM TripSeat ts
        JOIN FETCH ts.seat
        WHERE ts.trip.tripId = :tripId
        AND ts.tripSeatId IN :tripSeatIds
    """)
    List<TripSeat> findByTripIdAndTripSeatIdInWithSeat(@Param("tripId") Integer tripId,
                                                       @Param("tripSeatIds") List<Integer> tripSeatIds);

    @Modifying
    @Query("""
        UPDATE TripSeat ts
        SET ts.status = :status
        WHERE ts.tripSeatId IN :tripSeatIds
    """)
    int updateStatusByTripSeatIds(@Param("tripSeatIds") List<Integer> tripSeatIds,
                                  @Param("status") TripSeatStatus status);

    @Query(value = """
        SELECT TOP 1 ts.price
        FROM trip_seat ts
        WHERE ts.tripId = :tripId
          AND ts.price IS NOT NULL
        ORDER BY ts.tripSeatId ASC
        """, nativeQuery = true)
    Optional<BigDecimal> findFirstSeatPriceByTripId(@Param("tripId") Integer tripId);

}
