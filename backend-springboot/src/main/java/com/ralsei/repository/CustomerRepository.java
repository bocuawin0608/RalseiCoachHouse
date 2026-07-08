package com.ralsei.repository;

import com.ralsei.model.Customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByAccountId(Integer accountId);
    boolean existsByPhone(String phone);

    /**
     * Prevents two active customer profiles from sharing the same email during
     * customer self-service profile updates.
     */
    boolean existsByEmailIgnoreCaseAndCustomerIdNot(String email, Integer customerId);
}
