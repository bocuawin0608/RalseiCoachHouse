package com.ralsei.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.passengerbooking.PassengerProfileProjection;
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

    @Query("""
        SELECT COUNT(ptd) > 0
        FROM PassengerTicketDetail ptd
        JOIN PassengerTicket pt ON pt.passengerTicketId = ptd.passengerTicketId
        JOIN Payment p ON p.passengerTicketId = pt.passengerTicketId
        WHERE ptd.phone = :phone
          AND pt.status = 'CONFIRMED'
          AND ptd.status = 'CONFIRMED'
          AND p.status = 'COMPLETED'
    """)
    boolean existsConfirmedPaidByPhone(@Param("phone") String phone);

    @Query("""
        SELECT ptd.fullName AS fullName, ptd.email AS email
        FROM PassengerTicketDetail ptd
        JOIN PassengerTicket pt ON pt.passengerTicketId = ptd.passengerTicketId
        JOIN Payment p ON p.passengerTicketId = pt.passengerTicketId
        WHERE ptd.phone = :phone
          AND pt.status = 'CONFIRMED'
          AND ptd.status = 'CONFIRMED'
          AND p.status = 'COMPLETED'
        ORDER BY p.paymentTime DESC
    """)
    List<PassengerProfileProjection> findLatestConfirmedProfilesByPhone(
            @Param("phone") String phone,
            Pageable pageable);
}
