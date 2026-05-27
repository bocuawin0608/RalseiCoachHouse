package com.ralsei.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ralsei.dto.projection.SeatLayoutProjection;
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
        WHERE (:seatLayoutName IS NULL OR sl.seatLayoutName LIKE '%' + :seatLayoutName + '%')
            AND (:isActive IS NULL OR sl.isActive = :isActive)
            AND (:minPrice IS NULL OR p.seatPrice >= :minPrice)
            AND (:maxPrice IS NULL OR p.seatPrice <= :maxPrice)
            AND (:minSeats IS NULL OR sl.totalSeat >= :minSeats)
            AND (:maxSeats IS NULL OR sl.totalSeat <= :maxSeats)
        """, 
        nativeQuery = true,
        countQuery = """
            SELECT COUNT(DISTINCT sl.seatLayoutId)
            FROM seat_layout sl
            LEFT JOIN seat_layout_price p
                ON p.seatLayoutId = sl.seatLayoutId
                AND :now BETWEEN p.startEffectiveDate AND p.endEffectiveDate
            WHERE (:seatLayoutName IS NULL OR sl.seatLayoutName LIKE '%' + :seatLayoutName + '%')
                AND (:isActive IS NULL OR sl.isActive = :isActive)
                AND (:minPrice IS NULL OR p.seatPrice >= :minPrice)
                AND (:maxPrice IS NULL OR p.seatPrice <= :maxPrice)
                AND (:minSeats IS NULL OR sl.totalSeat >= :minSeats)
                AND (:maxSeats IS NULL OR sl.totalSeat <= :maxSeats)
        """)
    Page<SeatLayoutProjection> searchSeatLayouts(
        @Param("seatLayoutName") String seatLayoutName,
        @Param("isActive") Boolean isActive,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minSeats") Integer minSeats,
        @Param("maxSeats") Integer maxSeats,
        @Param("now") LocalDateTime now,
        Pageable pageable);
}
