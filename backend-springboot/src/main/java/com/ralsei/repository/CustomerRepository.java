package com.ralsei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.CustomerListProjection;
import com.ralsei.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByAccountId(Integer accountId);
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndCustomerIdNot(String phone, Integer customerId);
    boolean existsByEmailIgnoreCaseAndCustomerIdNot(String email, Integer customerId);

    @Query(value = """
        SELECT c.customerId                                   AS customerId,
            c.customerName                                    AS customerName,
            c.phone                                           AS phone,
            c.email                                           AS email,
            c.dob                                             AS dob,
            c.isActive                                        AS isActive,
            c.createdAt                                       AS createdAt,
            c.accountId                                       AS accountId,
            COUNT(pt.passengerTicketId)                       AS totalTrips,
            COALESCE(SUM(pt.totalPrice), 0)                   AS totalSpent,
            MAX(pt.createdAt)                                 AS lastBooking
        FROM customer c
        LEFT JOIN passenger_ticket pt ON pt.customerId = c.customerId
        WHERE (:search IS NULL
            OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(ISNULL(c.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(ISNULL(c.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isActive IS NULL OR c.isActive = :isActive)
          AND (:accountType IS NULL
              OR (:accountType = 'registered' AND c.accountId IS NOT NULL)
              OR (:accountType = 'crm' AND c.accountId IS NULL))
        GROUP BY c.customerId, c.customerName, c.phone, c.email, c.dob, c.isActive, c.createdAt, c.accountId
        HAVING (:activity IS NULL
            OR (:activity = 'never_booked' AND COUNT(pt.passengerTicketId) = 0)
            OR (:activity = 'inactive_3mo' AND COUNT(pt.passengerTicketId) > 0
                AND (MAX(pt.createdAt) IS NULL OR MAX(pt.createdAt) < DATEADD(MONTH, -3, GETDATE())))
            OR (:activity = 'active' AND COUNT(pt.passengerTicketId) > 0
                AND (MAX(pt.createdAt) >= DATEADD(MONTH, -3, GETDATE()))))
        ORDER BY c.customerId DESC
    """, nativeQuery = true)
    List<CustomerListProjection> filterCustomers(
        @Param("search") String search,
        @Param("isActive") Boolean isActive,
        @Param("accountType") String accountType,
        @Param("activity") String activity
    );

    @Query(value = "SELECT COUNT(*) FROM passenger_ticket WHERE customerId = :customerId", nativeQuery = true)
    long countTicketsByCustomerId(@Param("customerId") Integer customerId);
}
