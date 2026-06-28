package com.ralsei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.PassengerTicketDetail;

public interface PassengerTicketDetailRepository extends JpaRepository<PassengerTicketDetail, Integer> {

    @Modifying
    @Query("""
        UPDATE PassengerTicketDetail ptd
        SET ptd.status = :status
        WHERE ptd.passengerTicketId = :passengerTicketId
    """)
    int updateStatusByPassengerTicketId(@Param("passengerTicketId") Integer passengerTicketId,
                                        @Param("status") String status);

    @Query("""
        SELECT ptd.tripSeatId
        FROM PassengerTicketDetail ptd
        WHERE ptd.passengerTicketId = :passengerTicketId
        AND ptd.tripSeatId IS NOT NULL
    """)
    List<Integer> findTripSeatIdsByPassengerTicketId(@Param("passengerTicketId") Integer passengerTicketId);

    List<PassengerTicketDetail> findByPassengerTicketId(Integer passengerTicketId);
}
