package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CargoTicket;

public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer>{
    boolean existsByTicketCodeIgnoreCase(String ticketCode);

    boolean existsByTicketCodeIgnoreCaseAndCargoTicketIdNot(String ticketCode, int cargoTicketId);
}
