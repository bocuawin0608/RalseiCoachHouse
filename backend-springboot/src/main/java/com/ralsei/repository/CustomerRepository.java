package com.ralsei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.CustomerListProjection;
import com.ralsei.model.Customer;

/**
 * Repository interface for {@link com.ralsei.model.Customer} entity.
 */

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByAccountId(Integer accountId);
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndCustomerIdNot(String phone, Integer customerId);

    @Query(value = """
        SELECT c.customerId   AS customerId,
            c.customerName AS customerName,
            c.phone        AS phone,
            c.email        AS email,
            c.dob          AS dob,
            c.isActive     AS isActive,
            c.createdAt    AS createdAt
        FROM customer c
        WHERE (:search IS NULL
            OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(ISNULL(c.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(ISNULL(c.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isActive IS NULL OR c.isActive = :isActive)
        ORDER BY c.customerId DESC
    """, nativeQuery = true)
    List<CustomerListProjection> filterCustomers(@Param("search") String search, @Param("isActive") Boolean isActive);

    @Query(value = "SELECT COUNT(*) FROM passenger_ticket WHERE customerId = :customerId", nativeQuery = true)
    long countTicketsByCustomerId(@Param("customerId") Integer customerId);
}
