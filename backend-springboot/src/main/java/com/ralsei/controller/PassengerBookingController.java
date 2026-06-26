package com.ralsei.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.passengerbooking.PriceCalculationRequest;
import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.response.passengerbooking.PriceCalculationResponse;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.Step2InitResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.service.passengerbooking.PassengerBookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Validated
public class PassengerBookingController {
    
    private final PassengerBookingService bookingService;

    @GetMapping("/trips/{tripId}/seats")
    @PreAuthorize("isAnonymous() or hasRole('CUSTOMER')")
    public ResponseEntity<List<TripSeatResponse>> getSeatMap(@PathVariable @Min(value = 1, message = "ID của Chuyến phải lớn hơn 0.") Integer tripId) {
        return ResponseEntity.ok(bookingService.getSeatMap(tripId));
    }

    @PostMapping("/trips/{tripId}/seats/lock")
    @PreAuthorize("isAnonymous() or hasRole('CUSTOMER')")
    public ResponseEntity<SeatLockResponse> lockSeats(
        @PathVariable @Min(value = 1, message = "ID của Chuyến phải lớn hơn 0.") Integer tripId,
        @Valid @RequestBody SeatLockRequest request,
        @RequestHeader("X-Booking-Session") String holdToken
    ) {
        return ResponseEntity.ok(bookingService.lockSeats(tripId, request, holdToken));
    }

    @PostMapping("/trips/{tripId}/seats/release")
    @PreAuthorize("isAnonymous() or hasRole('CUSTOMER')")
    public ResponseEntity<Boolean> releaseSeats(
        @PathVariable @Min(value = 1, message = "ID chuyến xe phải lớn hơn 0.") Integer tripId,
        @Valid @RequestBody SeatLockRequest request, 
        @RequestHeader("X-Booking-Session") String holdToken
    ) {
        return ResponseEntity.ok(bookingService.releaseSeats(request.tripSeatIds(), holdToken)); 
    }

    @PostMapping(
        path = "/trips/{tripId:\\d+}/seats/release/beacon",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseSeatsByBeacon(
        @PathVariable @Min(value = 1, message = "ID chuyến xe phải lớn hơn 0.") Integer tripId,
        @RequestParam("session") String holdToken
    ) {
        bookingService.releaseSeatsByBecon(holdToken);
    }

    @GetMapping("/trips/{tripId}/step2-init-data")
    @PreAuthorize("isAnonymous() or hasRole('CUSTOMER')")
    public ResponseEntity<Step2InitResponse> getStep2InitData(
        @PathVariable @Min(value = 1, message = "ID của Chuyến phải lớn hơn 0.") Integer tripId,
        @RequestHeader("X-Booking-Session") String holdToken,
        @RequestHeader(value ="Authorization", required=false) String accessToken
    ) {
        return ResponseEntity.ok(bookingService.getStep2InitData(tripId, holdToken, accessToken));
    }

    @PostMapping("/trips/{tripId}/calculate-price")
    @PreAuthorize("isAnonymous() or hasRole('CUSTOMER')")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(
        @PathVariable @Min(value = 1, message = "ID của Chuyến phải lớn hơn 0.") Integer tripId,
        @RequestBody PriceCalculationRequest request,
        @RequestHeader("X-Booking-Session") String holdToken,
        @RequestHeader(value ="Authorization", required=false) String accessToken
    ) {
        PriceCalculationRequest fullRequest = new PriceCalculationRequest(
            holdToken,
            request.pickupStopId(),
            request.dropoffStopId(),
            request.voucherId()
        );
        return ResponseEntity.ok(bookingService.calculatePrice(tripId, fullRequest, accessToken));
    }
}
