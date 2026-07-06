package com.ralsei.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.response.CargoHistoryListResponse;
import com.ralsei.dto.response.CargoTrackingResponse;
import com.ralsei.service.CargoTrackingService;
import com.ralsei.util.JwtAccountIdExtractor;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for cargo tracking operations.
 */
@RestController
@RequestMapping("/api/v1/cargo-tracking")
@RequiredArgsConstructor
public class CargoTrackingController {

    private final CargoTrackingService cargoTrackingService;

    /**
     * Looks up a cargo order by its public ticket code (unauthenticated).
     */
    @GetMapping("/{ticketCode}")
    public ResponseEntity<CargoTrackingResponse> trackByCode(@PathVariable String ticketCode) {
        CargoTrackingResponse response = cargoTrackingService.trackByCode(ticketCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a list of cargo orders belonging to the authenticated customer, optionally filtered by status.
     */
    @GetMapping("/my-cargo")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CargoHistoryListResponse>> getMyCargo(
        @RequestParam(required = false) String status
    ) {
        String username = JwtAccountIdExtractor.getCurrentUsername();
        List<CargoHistoryListResponse> list = cargoTrackingService.getMyCargoHistory(username, status);
        return ResponseEntity.ok(list);
    }

    /**
     * Returns full detail for a specific cargo order owned by the authenticated customer.
     */
    @GetMapping("/my-cargo/{cargoTicketId:\\d+}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CargoTrackingResponse> getMyCargoDetail(
        @PathVariable @Min(value = 1, message = "ID đơn hàng phải lớn hơn 0.") Integer cargoTicketId
    ) {
        String username = JwtAccountIdExtractor.getCurrentUsername();
        CargoTrackingResponse detail = cargoTrackingService.getCargoDetail(cargoTicketId);
        return ResponseEntity.ok(detail);
    }
}
