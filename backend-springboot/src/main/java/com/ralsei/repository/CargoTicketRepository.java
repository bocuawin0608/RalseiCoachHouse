package com.ralsei.repository;

import com.ralsei.model.CargoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer> {
    Optional<CargoTicket> findByTicketCode(String ticketCode);
}
