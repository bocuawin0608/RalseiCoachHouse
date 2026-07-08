package com.ralsei.repository;

import com.ralsei.model.Customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import com.ralsei.dto.projection.cargoticket.CargoTicketCustomerOptionProjection;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query(value = """
            SELECT customerId AS customerId, customerName AS customerName
            FROM customer WHERE isActive = 1 ORDER BY customerName
            """, nativeQuery = true)
    java.util.List<CargoTicketCustomerOptionProjection> findCargoTicketCustomerOptions();

    Optional<Customer> findByAccountId(Integer accountId);

    boolean existsByPhone(String phone);

    /**
     * Prevents two active customer profiles from sharing the same email during
     * customer self-service profile updates.
     */
    boolean existsByEmailIgnoreCaseAndCustomerIdNot(String email, Integer customerId);
}
