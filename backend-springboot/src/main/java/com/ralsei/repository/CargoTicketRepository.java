package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.CargoTicket;

public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer> {
    @Query(value = "SELECT TOP 10 phone, name FROM (SELECT senderPhone as phone, senderName as name FROM cargo_ticket WHERE senderPhone LIKE %:phone% UNION SELECT receiverPhone as phone, receiverName as name FROM cargo_ticket WHERE receiverPhone LIKE %:phone%) AS Contacts", nativeQuery = true)
    List<Object[]> findContactsByPhoneNative(@Param("phone") String phone);

    boolean existsByTicketCodeIgnoreCase(String ticketCode);

    boolean existsByTicketCodeIgnoreCaseAndCargoTicketIdNot(String ticketCode, int cargoTicketId);

    org.springframework.data.domain.Page<CargoTicket> findByStatusNot(String status, Pageable pageable);

    Optional<CargoTicket> findByCargoTicketIdAndStatusNot(int cargoTicketId, String status);

    // của duc
    Optional<CargoTicket> findByTicketCode(String ticketCode);

    // của duc
    List<CargoTicket> findByTripId(int tripId);

    // của duc
    @Query("SELECT ct FROM CargoTicket ct WHERE ct.tripId = :tripId AND ct.status IN :statuses ORDER BY ct.cargoTicketId ASC")
    List<CargoTicket> findByTripIdAndStatusIn(@Param("tripId") int tripId, @Param("statuses") List<String> statuses);
}
