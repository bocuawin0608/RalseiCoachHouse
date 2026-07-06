package com.ralsei.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.passengerbooking.PassengerProfileProjection;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.dto.projection.customer.CustomerTicketHistoryProjection;

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

    /**
     * Checks whether a phone number belongs to a fully paid, confirmed passenger.
     * Pending and failed payments must not bypass OTP verification.
     *
     * @param phone normalized local-format phone number
     * @return {@code true} when at least one confirmed paid ticket exists
     */
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

    /**
     * Loads confirmed passenger profile snapshots with the latest payment first.
     * The caller supplies a one-row page when only the newest profile is needed.
     *
     * @param phone normalized local-format phone number
     * @param pageable result limit and offset
     * @return confirmed passenger profile snapshots
     */
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
        Pageable pageable
    );

    /**
     * Loads all ticket seats belonging to one account for customer history.
     * Ownership is resolved in SQL and is never trusted from a client-provided ID.
     */
    @Query(value = """
        SELECT pt.passengerTicketId AS passengerTicketId,
               ptd.ticketDetailId AS ticketDetailId,
               pt.ticketCode AS ticketCode,
               CAST(pt.status AS VARCHAR(30)) AS ticketStatus,
               pt.totalPrice AS totalPrice,
               pt.pickupStopName AS pickupStopName,
               pt.dropoffStopName AS dropoffStopName,
               pt.createdAt AS bookedAt,
               t.departureTime AS departureTime,
               r.routeName AS routeName,
               ct.coachTypeName AS coachTypeName,
               pay.paymentMethod AS paymentMethod,
               pay.status AS paymentStatus,
               ptd.fullName AS fullName,
               ptd.phone AS phone,
               ptd.email AS email,
               ptd.seatCodeSnapshot AS seatCode,
               ptd.price AS seatPrice
        FROM passenger_ticket_detail ptd
        JOIN passenger_ticket pt ON pt.passengerTicketId = ptd.passengerTicketId
        JOIN customer c ON c.customerId = pt.customerId
        JOIN trip t ON t.tripId = pt.tripId
        JOIN route r ON r.routeId = t.routeId
        JOIN coach co ON co.coachId = t.coachId
        JOIN coach_type ct ON ct.coachTypeId = co.coachTypeId
        LEFT JOIN payment pay ON pay.passengerTicketId = pt.passengerTicketId
        WHERE c.accountId = :accountId
          AND (:ticketCode IS NULL OR pt.ticketCode = :ticketCode)
        ORDER BY t.departureTime DESC, pt.passengerTicketId DESC, ptd.ticketDetailId ASC
        """, nativeQuery = true)
    List<CustomerTicketHistoryProjection> findCustomerTicketHistory(
        @Param("accountId") Integer accountId,
        @Param("ticketCode") String ticketCode
    );

    /** Returns a boarding token only when the requested seat belongs to the account. */
    @Query(value = """
        SELECT ptd.qrcode
        FROM passenger_ticket_detail ptd
        JOIN passenger_ticket pt ON pt.passengerTicketId = ptd.passengerTicketId
        JOIN customer c ON c.customerId = pt.customerId
        WHERE ptd.ticketDetailId = :ticketDetailId
          AND c.accountId = :accountId
          AND pt.status = 'CONFIRMED'
          AND ptd.status = 'CONFIRMED'
        """, nativeQuery = true)
    java.util.Optional<String> findOwnedQrToken(
        @Param("ticketDetailId") Integer ticketDetailId,
        @Param("accountId") Integer accountId
    );

}
