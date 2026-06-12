package com.ralsei.repository;

import com.ralsei.model.PassengerTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerTicketRepository extends JpaRepository<PassengerTicket, Integer> {
    boolean existsByVoucherId(int voucherId);

    long countByVoucherId(int voucherId);
}
