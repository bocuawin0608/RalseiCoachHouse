package com.ralsei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CargoTicket;

public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer> {
    Optional<CargoTicket> findByTicketCode(String ticketCode);

    List<CargoTicket> findByTripId(int tripId);
}