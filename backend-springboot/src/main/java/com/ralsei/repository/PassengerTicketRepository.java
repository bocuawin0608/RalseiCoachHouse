package com.ralsei.repository;

import java.util.Set;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.PassengerTicket;
import com.ralsei.model.enums.PassengerTicketMajorChangeType;
import com.ralsei.model.enums.PassengerTicketStatus;

/**
 * Provides persistence access for passenger ticket data.
 */
public interface PassengerTicketRepository extends JpaRepository<PassengerTicket, Integer> {
    boolean existsByVoucherId(int voucherId);

    boolean existsByTicketCode(String ticketCode);

    long countByVoucherId(int voucherId);

    //TODO: maybe phải sửa query nhẹ nếu sau dùng relationship annotation bên PassengerTicket.java
    @Query(value = """
       SELECT DISTINCT pt.voucherId
       FROM PassengerTicket pt
       JOIN Customer c ON pt.customerId = c.customerId
       WHERE c.accountId = :accountId 
            AND pt.status != :cancelledStatus  
            AND pt.voucherId IS NOT NULL
    """)
    Set<Integer> getUsedVoucherIdsByAccountId(@Param("accountId") Integer accountId,
                                              @Param("cancelledStatus") PassengerTicketStatus cancelledStatus);

    @Modifying
    @Query("""
        UPDATE PassengerTicket pt
        SET pt.status = :newStatus
        WHERE pt.passengerTicketId = :passengerTicketId
        AND pt.status = :expectedStatus
    """)
    int updateStatusIfCurrent(@Param("passengerTicketId") Integer passengerTicketId,
                              @Param("expectedStatus") PassengerTicketStatus expectedStatus,
                              @Param("newStatus") PassengerTicketStatus newStatus);

    @Modifying
    @Query("""
        UPDATE PassengerTicket pt
        SET pt.majorChangeType = :majorChangeType
        WHERE pt.passengerTicketId = :passengerTicketId
        AND pt.majorChangeType IS NULL
    """)
    int markMajorChangeIfUnused(@Param("passengerTicketId") Integer passengerTicketId,
                                @Param("majorChangeType") PassengerTicketMajorChangeType majorChangeType);

    List<PassengerTicket> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    @Query("""
        SELECT COUNT(pt) > 0
        FROM PassengerTicket pt
        JOIN Payment p ON p.passengerTicketId = pt.passengerTicketId
        WHERE p.transactionId = :transactionId
        AND pt.customerId = :customerId
        AND p.status = 'PENDING'
    """)
    boolean existsPendingPaymentByTransactionIdAndCustomerId(
            @Param("transactionId") String transactionId,
            @Param("customerId") Integer customerId);

    @Query(value = """
        SELECT COUNT(DISTINCT pt.passengerTicketId)
        FROM passenger_ticket pt
        INNER JOIN trip t ON t.tripId = pt.tripId
        INNER JOIN passenger_ticket_detail ptd ON ptd.passengerTicketId = pt.passengerTicketId
        LEFT JOIN customer c ON c.customerId = pt.customerId
        WHERE (:phone IS NULL OR ptd.phone LIKE CONCAT('%', :phone, '%') OR c.phone LIKE CONCAT('%', :phone, '%'))
          AND (:ticketCode IS NULL OR pt.ticketCode LIKE CONCAT(:ticketCode, '%'))
          AND (:status IS NULL OR pt.status = :status)
          AND (:routeId IS NULL OR t.routeId = :routeId)
          AND (:tripId IS NULL OR pt.tripId = :tripId)
          AND (:departureDate IS NULL OR (
                t.departureTime >= CAST(:departureDate AS DATETIME2)
                AND t.departureTime < DATEADD(day, 1, CAST(:departureDate AS DATETIME2))
              ))
        """, nativeQuery = true)
    long countStaffPassengerTickets(
        @Param("phone") String phone,
        @Param("ticketCode") String ticketCode,
        @Param("status") String status,
        @Param("routeId") Integer routeId,
        @Param("tripId") Integer tripId,
        @Param("departureDate") java.time.LocalDate departureDate
    );

    @Query(value = """
        SELECT pt.passengerTicketId
        FROM passenger_ticket pt
        INNER JOIN trip t ON t.tripId = pt.tripId
        INNER JOIN passenger_ticket_detail ptd ON ptd.passengerTicketId = pt.passengerTicketId
        LEFT JOIN customer c ON c.customerId = pt.customerId
        WHERE (:phone IS NULL OR ptd.phone LIKE CONCAT('%', :phone, '%') OR c.phone LIKE CONCAT('%', :phone, '%'))
          AND (:ticketCode IS NULL OR pt.ticketCode LIKE CONCAT(:ticketCode, '%'))
          AND (:status IS NULL OR pt.status = :status)
          AND (:routeId IS NULL OR t.routeId = :routeId)
          AND (:tripId IS NULL OR pt.tripId = :tripId)
          AND (:departureDate IS NULL OR (
                t.departureTime >= CAST(:departureDate AS DATETIME2)
                AND t.departureTime < DATEADD(day, 1, CAST(:departureDate AS DATETIME2))
              ))
        GROUP BY pt.passengerTicketId, t.departureTime
        ORDER BY t.departureTime DESC, pt.passengerTicketId DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """, nativeQuery = true)
    List<Integer> findStaffPassengerTicketIds(
        @Param("phone") String phone,
        @Param("ticketCode") String ticketCode,
        @Param("status") String status,
        @Param("routeId") Integer routeId,
        @Param("tripId") Integer tripId,
        @Param("departureDate") java.time.LocalDate departureDate,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
}
