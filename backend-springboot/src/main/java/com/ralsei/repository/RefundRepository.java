package com.ralsei.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.staffrefund.StaffPassengerRefundRowProjection;
import com.ralsei.model.Refund;

/**
 * Persistence boundary for customer and staff refund workflows.
 */
public interface RefundRepository extends JpaRepository<Refund, Integer> {

    /** Prevents duplicate active refunds for the same completed payment. */
    boolean existsByPaymentIdAndStatusIn(Integer paymentId, Collection<String> statuses);

    List<Refund> findByPaymentIdOrderByCreatedAtDesc(Integer paymentId);

    /**
     * Counts passenger refunds joined through {@code refund -> payment -> passenger_ticket}.
     * Results are ordered by {@code r.createdAt DESC} in the list query companion method.
     */
    @Query(value = """
        SELECT COUNT(DISTINCT r.refundId)
        FROM refund r
        INNER JOIN payment p ON p.paymentId = r.paymentId
        INNER JOIN passenger_ticket pt ON pt.passengerTicketId = p.passengerTicketId
        WHERE p.passengerTicketId IS NOT NULL
          AND (:status IS NULL OR r.status = :status)
          AND (:ticketCode IS NULL OR pt.ticketCode LIKE CONCAT(:ticketCode, '%'))
          AND (:phone IS NULL OR EXISTS (
                SELECT 1
                FROM passenger_ticket_detail ptdPhone
                LEFT JOIN customer c ON c.customerId = pt.customerId
                WHERE ptdPhone.passengerTicketId = pt.passengerTicketId
                  AND (ptdPhone.phone LIKE CONCAT('%', :phone, '%')
                       OR c.phone LIKE CONCAT('%', :phone, '%'))
              ))
          AND (:createdFrom IS NULL OR CAST(r.createdAt AS DATE) >= :createdFrom)
          AND (:createdTo IS NULL OR CAST(r.createdAt AS DATE) <= :createdTo)
        """, nativeQuery = true)
    long countPassengerRefunds(
        @Param("status") String status,
        @Param("ticketCode") String ticketCode,
        @Param("phone") String phone,
        @Param("createdFrom") LocalDate createdFrom,
        @Param("createdTo") LocalDate createdTo
    );

    /**
     * Returns passenger refund identifiers sorted by newest request first.
     */
    @Query(value = """
        SELECT r.refundId
        FROM refund r
        INNER JOIN payment p ON p.paymentId = r.paymentId
        INNER JOIN passenger_ticket pt ON pt.passengerTicketId = p.passengerTicketId
        WHERE p.passengerTicketId IS NOT NULL
          AND (:status IS NULL OR r.status = :status)
          AND (:ticketCode IS NULL OR pt.ticketCode LIKE CONCAT(:ticketCode, '%'))
          AND (:phone IS NULL OR EXISTS (
                SELECT 1
                FROM passenger_ticket_detail ptdPhone
                LEFT JOIN customer c ON c.customerId = pt.customerId
                WHERE ptdPhone.passengerTicketId = pt.passengerTicketId
                  AND (ptdPhone.phone LIKE CONCAT('%', :phone, '%')
                       OR c.phone LIKE CONCAT('%', :phone, '%'))
              ))
          AND (:createdFrom IS NULL OR CAST(r.createdAt AS DATE) >= :createdFrom)
          AND (:createdTo IS NULL OR CAST(r.createdAt AS DATE) <= :createdTo)
        GROUP BY r.refundId, r.createdAt
        ORDER BY r.createdAt DESC, r.refundId DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """, nativeQuery = true)
    List<Integer> findPassengerRefundIds(
        @Param("status") String status,
        @Param("ticketCode") String ticketCode,
        @Param("phone") String phone,
        @Param("createdFrom") LocalDate createdFrom,
        @Param("createdTo") LocalDate createdTo,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * Loads flat passenger refund rows for the supplied refund identifiers.
     */
    @Query(value = """
        SELECT
            r.refundId AS refundId,
            r.paymentId AS paymentId,
            p.passengerTicketId AS passengerTicketId,
            r.amount AS amount,
            r.reason AS reason,
            r.refundMethod AS refundMethod,
            r.transactionId AS transactionId,
            r.status AS status,
            r.refundTime AS refundTime,
            r.callbackData AS callbackData,
            r.createdAt AS createdAt,
            r.createdBy AS createdBy,
            r.updatedAt AS updatedAt,
            r.updatedBy AS updatedBy,
            pt.ticketCode AS ticketCode,
            primaryPassenger.fullName AS customerName,
            primaryPassenger.phone AS customerPhone
        FROM refund r
        INNER JOIN payment p ON p.paymentId = r.paymentId
        INNER JOIN passenger_ticket pt ON pt.passengerTicketId = p.passengerTicketId
        OUTER APPLY (
            SELECT TOP 1 ptd.fullName, ptd.phone
            FROM passenger_ticket_detail ptd
            WHERE ptd.passengerTicketId = pt.passengerTicketId
            ORDER BY ptd.ticketDetailId
        ) primaryPassenger
        WHERE r.refundId IN (:refundIds)
        ORDER BY r.createdAt DESC, r.refundId DESC
        """, nativeQuery = true)
    List<StaffPassengerRefundRowProjection> findPassengerRefundRowsByRefundIds(
        @Param("refundIds") List<Integer> refundIds
    );

    /**
     * Loads one passenger refund row for staff detail review.
     */
    @Query(value = """
        SELECT
            r.refundId AS refundId,
            r.paymentId AS paymentId,
            p.passengerTicketId AS passengerTicketId,
            r.amount AS amount,
            r.reason AS reason,
            r.refundMethod AS refundMethod,
            r.transactionId AS transactionId,
            r.status AS status,
            r.refundTime AS refundTime,
            r.callbackData AS callbackData,
            r.createdAt AS createdAt,
            r.createdBy AS createdBy,
            r.updatedAt AS updatedAt,
            r.updatedBy AS updatedBy,
            pt.ticketCode AS ticketCode,
            primaryPassenger.fullName AS customerName,
            primaryPassenger.phone AS customerPhone
        FROM refund r
        INNER JOIN payment p ON p.paymentId = r.paymentId
        INNER JOIN passenger_ticket pt ON pt.passengerTicketId = p.passengerTicketId
        OUTER APPLY (
            SELECT TOP 1 ptd.fullName, ptd.phone
            FROM passenger_ticket_detail ptd
            WHERE ptd.passengerTicketId = pt.passengerTicketId
            ORDER BY ptd.ticketDetailId
        ) primaryPassenger
        WHERE r.refundId = :refundId
          AND p.passengerTicketId IS NOT NULL
        """, nativeQuery = true)
    StaffPassengerRefundRowProjection findPassengerRefundRowByRefundId(@Param("refundId") int refundId);
}
