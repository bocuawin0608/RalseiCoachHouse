package com.ralsei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ralsei.dto.projection.CargoHistoryListProjection;
import com.ralsei.model.CargoTicket;

@Repository
public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer> {
    /** Finds a cargo order by its public ticket code. */
    Optional<CargoTicket> findByTicketCode(String ticketCode);

    /**
     * Lists cargo orders where the given customer is the sender (customerId)
     * or the receiver (receiverPhone), with optional status group filter.
     */
    @Query(value = """
        SELECT ct.cargoTicketId   AS cargoTicketId,
            ct.ticketCode          AS ticketCode,
            ct.status              AS status,
            ct.senderName          AS senderName,
            ct.senderPhone         AS senderPhone,
            ct.receiverName        AS receiverName,
            ct.receiverPhone       AS receiverPhone,
            ct.totalPrice          AS totalPrice,
            ct.createdAt           AS createdAt,
            csPickup.stopPointName AS pickupStopName,
            csDropoff.stopPointName AS dropoffStopName,
            r.routeName            AS tripRouteName,
            t.departureTime        AS tripDepartureTime
        FROM cargo_ticket ct
        JOIN coach_stop csPickup ON csPickup.stopPointId = ct.pickupStopId
        JOIN coach_stop csDropoff ON csDropoff.stopPointId = ct.dropoffStopId
        LEFT JOIN trip t ON t.tripId = ct.tripId
        LEFT JOIN route r ON r.routeId = t.routeId
        WHERE ct.customerId = :customerId OR ct.receiverPhone = :receiverPhone
        AND (:status IS NULL OR
            (:status = 'ONGOING' AND ct.status IN ('RECEIVED', 'LOADED', 'ARRIVED'))
            OR (:status = 'COMPLETED' AND ct.status = 'DELIVERED')
            OR (:status = 'CANCELLED' AND ct.status IN ('CANCELLED', 'REJECTED', 'ABANDONED')))
        ORDER BY ct.createdAt DESC
    """, nativeQuery = true)
    List<CargoHistoryListProjection> findHistoryByCustomerIdOrReceiverPhone(
        @Param("customerId") Integer customerId,
        @Param("receiverPhone") String receiverPhone,
        @Param("status") String status
    );
}
