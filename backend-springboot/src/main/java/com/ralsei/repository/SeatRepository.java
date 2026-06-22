package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.Seat;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    @Modifying
    @Query(value="DELETE s FROM seat s WHERE s.coachId = :coachId",nativeQuery=true)
    void bulkDeleteByCoachId(@Param("coachId") Integer coachId);
}
