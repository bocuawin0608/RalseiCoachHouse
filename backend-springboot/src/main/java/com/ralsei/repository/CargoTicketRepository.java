package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CargoTicket;

public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer>{
    boolean existsByTicketCodeIgnoreCase(String ticketCode);

    boolean existsByTicketCodeIgnoreCaseAndCargoTicketIdNot(String ticketCode, int cargoTicketId);

    org.springframework.data.domain.Page<CargoTicket> findByStatusNot(String status, org.springframework.data.domain.Pageable pageable);

    java.util.Optional<CargoTicket> findByCargoTicketIdAndStatusNot(int cargoTicketId, String status);
}
