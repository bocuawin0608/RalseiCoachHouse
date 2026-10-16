package com.ralsei.customer.controller.internal;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.customer.model.PassengerTicket;
import com.ralsei.customer.repository.PassengerTicketRepository;

@RestController
@RequestMapping("/api/internal/passenger-tickets")
public class InternalPassengerTicketController {

    private final PassengerTicketRepository passengerTicketRepository;

    public InternalPassengerTicketController(PassengerTicketRepository passengerTicketRepository) {
        this.passengerTicketRepository = passengerTicketRepository;
    }

    @GetMapping("/count-by-trip/{tripId}")
    public ResponseEntity<Integer> countTicketsByTripId(@PathVariable Integer tripId) {
        int count = (int) passengerTicketRepository.countByTripId(tripId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/by-trip/{tripId}")
    public ResponseEntity<List<PassengerTicket>> getTicketsByTripId(@PathVariable Integer tripId) {
        return ResponseEntity.ok(passengerTicketRepository.findByTripId(tripId));
    }
}
