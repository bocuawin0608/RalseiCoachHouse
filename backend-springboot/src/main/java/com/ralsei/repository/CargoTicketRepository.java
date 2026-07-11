package com.ralsei.repository;

import com.ralsei.model.CargoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer> {
    Optional<CargoTicket> findByTicketCode(String ticketCode);

    List<CargoTicket> findByTripId(int tripId);

    @Query("SELECT ct FROM CargoTicket ct WHERE ct.tripId = :tripId AND ct.status IN :statuses ORDER BY ct.cargoTicketId ASC")
    List<CargoTicket> findByTripIdAndStatusIn(@Param("tripId") int tripId, @Param("statuses") List<String> statuses);
}