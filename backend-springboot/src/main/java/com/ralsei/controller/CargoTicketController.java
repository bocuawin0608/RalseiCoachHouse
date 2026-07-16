package com.ralsei.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.request.cargoticket.CargoTicketWithDetailsRequest;
import com.ralsei.dto.request.cargoticketdetail.CargoTicketDetailRequest;
import com.ralsei.dto.request.cargoticket.TripByStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoOperationalTripPageResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.dto.response.cargoticket.CustomerContactResponse;
import com.ralsei.dto.response.cargoticket.TripByStopResponse;
import com.ralsei.dto.response.cargoticketdetail.CargoTicketDetailResponse;
import com.ralsei.service.CargoTicketService;
import com.ralsei.service.JwtService;
import com.ralsei.dto.response.cargoticketdetail.CargoTicketDetailPriceResponse;
import com.ralsei.dto.request.cargoticketdetail.CargoTicketDetailPriceRequest;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ticket-staff/cargo-tickets")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('TICKET_STAFF')")
/**
 * Handles HTTP requests for cargo ticket operations.
 */
public class CargoTicketController {
    private final CargoTicketService cargoTicketService;
    private final JwtService jwtService;

    @GetMapping("/form-options")
    public ResponseEntity<CargoTicketFormOptionsResponse> getFormOptions(
            @RequestParam(required = false) @Min(1) Integer pickupStopId,
            @RequestParam(required = false) @Min(1) Integer dropoffStopId) {
        return ResponseEntity.ok(cargoTicketService.getFormOptions(pickupStopId, dropoffStopId));
    }

    @GetMapping("/contacts/search")
    public ResponseEntity<List<CustomerContactResponse>> searchContacts(
            @RequestParam String phone) {
        return ResponseEntity.ok(cargoTicketService.searchContacts(phone));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CargoTicketResponse>> getCargoTickets(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(cargoTicketService.getAllCargoTickets(
                status, jwtService.extractAccountId(authorizationHeader), page, size));
    }

    /** Lists coaches that have not departed and can still accept cargo. */
    @GetMapping("/upcoming-trips")
    public ResponseEntity<CargoOperationalTripPageResponse> getUpcomingOperationalTrips(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "6") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(cargoTicketService.getUpcomingOperationalTrips(
                jwtService.extractAccountId(authorizationHeader), page, size));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<CargoTicketResponse> getCargoTicketById(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(cargoTicketService.getCargoTicketById(id));
    }

    @GetMapping("/{id:\\d+}/details")
    public ResponseEntity<List<CargoTicketDetailResponse>> getCargoTicketDetailsByTicketId(
            @PathVariable @Min(1) int id) {
        return ResponseEntity.ok(cargoTicketService.getCargoTicketDetailsByTicketId(id));
    }

    @PostMapping
    public ResponseEntity<CargoTicketResponse> createCargoTicket(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CargoTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cargoTicketService.createCargoTicket(
                request, jwtService.extractAccountId(authorizationHeader)));
    }

    @PostMapping("/with-details")
    public ResponseEntity<CargoTicketResponse> createCargoTicketWithDetails(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CargoTicketWithDetailsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cargoTicketService.createCargoTicketWithDetails(
                request, jwtService.extractAccountId(authorizationHeader)));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<CargoTicketResponse> updateCargoTicket(
            @PathVariable @Min(1) int id,
            @Valid @RequestBody CargoTicketRequest request) {
        return ResponseEntity.ok(cargoTicketService.updateCargoTicket(id, request));
    }

    /** Updates header and cargo rows in one transaction-backed operation. */
    @PutMapping("/{id:\\d+}/with-details")
    public ResponseEntity<CargoTicketResponse> updateCargoTicketWithDetails(
            @PathVariable @Min(1) int id,
            @Valid @RequestBody CargoTicketWithDetailsRequest request) {
        return ResponseEntity.ok(cargoTicketService.updateCargoTicketWithDetails(id, request));
    }

    @PostMapping("/{ticketId:\\d+}/details")
    public ResponseEntity<CargoTicketDetailResponse> createCargoTicketDetail(
            @PathVariable @Min(1) int ticketId,
            @Valid @RequestBody CargoTicketDetailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cargoTicketService.createCargoTicketDetail(ticketId, request));
    }

    @PutMapping("/details/{detailId:\\d+}")
    public ResponseEntity<CargoTicketDetailResponse> updateCargoTicketDetail(
            @PathVariable @Min(1) int detailId,
            @Valid @RequestBody CargoTicketDetailRequest request) {
        return ResponseEntity.ok(cargoTicketService.updateCargoTicketDetail(detailId, request));
    }

    @DeleteMapping("/details/{detailId:\\d+}")
    public ResponseEntity<Void> deleteCargoTicketDetail(@PathVariable @Min(1) int detailId) {
        cargoTicketService.deleteCargoTicketDetail(detailId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/disable")
    public ResponseEntity<Void> disable(@PathVariable @Min(1) int id) {
        cargoTicketService.disable(id);
        return ResponseEntity.noContent().build();
    }

    /** Confirms receiver hand-over for a package in ARRIVED state. */
    @PutMapping("/{id:\\d+}/confirm-received")
    public ResponseEntity<Void> confirmReceived(@PathVariable @Min(1) int id) {
        cargoTicketService.confirmReceived(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/complete-payment")
    public ResponseEntity<Void> completePayment(@PathVariable @Min(1) int id) {
        cargoTicketService.completePayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trips-by-stops")
    public ResponseEntity<List<TripByStopResponse>> getTripsByStops(
            @Valid @ModelAttribute TripByStopRequest request) {
        return ResponseEntity.ok(cargoTicketService.getTripsByStopsInOrder(request));
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<CargoTicketDetailPriceResponse> calculatePrice(
            @Valid @RequestBody CargoTicketDetailPriceRequest request) {
        return ResponseEntity.ok(cargoTicketService.calculatePrice(request));
    }
}
