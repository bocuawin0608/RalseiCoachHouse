package com.ralsei.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ralsei.dto.projection.SeatLayoutProjection;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
import com.ralsei.model.SeatLayout;

@Repository
public interface SeatLayoutRepository extends JpaRepository<SeatLayout, Integer>{
    @Query(value = """
        SELECT 
            sl.seatLayoutId AS seatLayoutId,
            sl.seatLayoutName AS seatLayoutName,
            sl.totalSeat AS totalSeat,
            sl.isActive AS isActive,
            p.seatPrice AS currentPrice
        FROM seat_layout sl
        LEFT JOIN seat_layout_price p 
            ON p.seatLayoutId = sl.seatLayoutId 
            AND :now BETWEEN p.startEffectiveDate AND p.endEffectiveDate
        WHERE (:#{#filter.seatLayoutName} IS NULL OR sl.seatLayoutName LIKE '%' + :#{#filter.seatLayoutName} + '%')
            AND (:#{#filter.isActive} IS NULL OR sl.isActive = :#{#filter.isActive})
            AND (:#{#filter.minPrice} IS NULL OR p.seatPrice >= :#{#filter.minPrice})
            AND (:#{#filter.maxPrice} IS NULL OR p.seatPrice <= :#{#filter.maxPrice})
            AND (:#{#filter.minSeats} IS NULL OR sl.totalSeat >= :#{#filter.minSeats})
            AND (:#{#filter.maxSeats} IS NULL OR sl.totalSeat <= :#{#filter.maxSeats})
        """, 
        nativeQuery = true,
        countQuery = """
            SELECT COUNT(DISTINCT sl.seatLayoutId)
            FROM seat_layout sl
            LEFT JOIN seat_layout_price p
                ON p.seatLayoutId = sl.seatLayoutId
                AND :now BETWEEN p.startEffectiveDate AND p.endEffectiveDate
            WHERE (:#{#filter.seatLayoutName} IS NULL OR sl.seatLayoutName LIKE '%' + :#{#filter.seatLayoutName} + '%')
                AND (:#{#filter.isActive} IS NULL OR sl.isActive = :#{#filter.isActive})
                AND (:#{#filter.minPrice} IS NULL OR p.seatPrice >= :#{#filter.minPrice})
                AND (:#{#filter.maxPrice} IS NULL OR p.seatPrice <= :#{#filter.maxPrice})
                AND (:#{#filter.minSeats} IS NULL OR sl.totalSeat >= :#{#filter.minSeats})
                AND (:#{#filter.maxSeats} IS NULL OR sl.totalSeat <= :#{#filter.maxSeats})
        """)
    Page<SeatLayoutProjection> searchSeatLayouts(
        @Param("filter") SeatLayoutFilterRequest filter,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query("""
        SELECT sl FROM SeatLayout sl
        LEFT JOIN FETCH sl.seats s WHERE sl.seatLayoutId = :id
        ORDER BY s.rowIndex ASC, s.colIndex ASC
        """)
    Optional<SeatLayout> findByIdWithSeatsAndPrice(Integer id);

}
