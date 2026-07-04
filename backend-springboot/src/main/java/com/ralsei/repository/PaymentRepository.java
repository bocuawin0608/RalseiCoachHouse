package com.ralsei.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByTransactionIdAndStatus(String transactionId, String status);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    Optional<Payment> findByPassengerTicketId(Integer passengerTicketId);

    @Modifying
    @Query("""
        UPDATE Payment p
        SET p.status = :newStatus
        WHERE p.transactionId = :transactionId
        AND p.status = :expectedStatus
    """)
    int updateStatusIfCurrent(@Param("transactionId") String transactionId,
                              @Param("expectedStatus") String expectedStatus,
                              @Param("newStatus") String newStatus);

    @Modifying
    @Query("""
        UPDATE Payment p
        SET p.status = :newStatus,
            p.paymentTime = :paymentTime,
            p.callbackData = :callbackData
        WHERE p.transactionId = :transactionId
        AND p.status = :expectedStatus
    """)
    int completeIfCurrent(@Param("transactionId") String transactionId,
                          @Param("expectedStatus") String expectedStatus,
                          @Param("newStatus") String newStatus,
                          @Param("paymentTime") LocalDateTime paymentTime,
                          @Param("callbackData") String callbackData);

    @Query("""
        SELECT DISTINCT p.transactionId
        FROM Payment p
        JOIN PassengerTicketDetail ptd ON ptd.passengerTicketId = p.passengerTicketId
        WHERE p.status = 'PENDING'
        AND p.passengerTicketId IS NOT NULL
        AND ptd.expiredAt IS NOT NULL
        AND ptd.expiredAt <= :now
    """)
    List<String> findOverduePendingPassengerTransactionIds(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
        SELECT COUNT(p) > 0
        FROM Payment p
        WHERE p.transactionId = :transactionId
        AND p.cancelToken = :cancelToken
        AND p.status = 'PENDING'
    """)
    boolean isValidCancelToken(@Param("transactionId") String transactionId,
                               @Param("cancelToken") String cancelToken);
}
