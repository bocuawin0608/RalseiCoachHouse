package com.ralsei.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.service.CargoTicketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ticket-staff/cargo-tickets")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('TICKET_STAFF')")
public class CargoTicketController {
    private final CargoTicketService cargoTicketService;

    @GetMapping("/form-options")
    public ResponseEntity<CargoTicketFormOptionsResponse> getFormOptions(
            @RequestParam(required = false) @Min(1) Integer pickupStopId,
            @RequestParam(required = false) @Min(1) Integer dropoffStopId) {
        return ResponseEntity.ok(cargoTicketService.getFormOptions(pickupStopId, dropoffStopId));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CargoTicketResponse>> getCargoTickets(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(cargoTicketService.getAllCargoTickets(page, size));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<CargoTicketResponse> getCargoTicketById(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(cargoTicketService.getCargoTicketById(id));
    }

    @PostMapping
    public ResponseEntity<CargoTicketResponse> createCargoTicket(
            @Valid @RequestBody CargoTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cargoTicketService.createCargoTicket(request));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<CargoTicketResponse> updateCargoTicket(
            @PathVariable @Min(1) int id,
            @Valid @RequestBody CargoTicketRequest request) {
        return ResponseEntity.ok(cargoTicketService.updateCargoTicket(id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteCargoTicket(@PathVariable @Min(1) int id) {
        cargoTicketService.deleteCargoTicket(id);
        return ResponseEntity.noContent().build();
    }
}
