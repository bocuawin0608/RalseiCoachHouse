package com.ralsei.repository;

import com.ralsei.model.Customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByAccountId(Integer accountId);
    boolean existsByPhone(String phone);
}
