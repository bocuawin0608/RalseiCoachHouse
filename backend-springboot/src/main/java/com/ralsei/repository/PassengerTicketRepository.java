package com.ralsei.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.PassengerTicket;
import com.ralsei.model.enums.PassengerTicketStatus;

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
}
