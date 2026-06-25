package com.ralsei.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
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
    public ResponseEntity<List<TripSeatResponse>> getSeatMap(@PathVariable @Min(value = 1, message = "ID của Chuyến phải lớn hơn 0.") Integer tripId) {
        return ResponseEntity.ok(bookingService.getSeatMap(tripId));
    }

    @PostMapping("/trips/{tripId}/seats/lock")
    public ResponseEntity<SeatLockResponse> lockSeats(
        @PathVariable @Min(value = 1, message = "ID của Chuyến phải lớn hơn 0.") Integer tripId,
        @Valid @RequestBody SeatLockRequest request,
        @RequestHeader("X-Booking-Session") String holdToken
    ) {
        return ResponseEntity.ok(bookingService.lockSeats(tripId, request, holdToken));
    }

    @PostMapping("/trips/{tripId}/seats/release")
    public ResponseEntity<Boolean> releaseSeats(
        @PathVariable @Min(value = 1, message = "ID chuyến xe phải lớn hơn 0.") Integer tripId,
        @Valid @RequestBody SeatLockRequest request, 
        @RequestHeader("X-Booking-Session") String holdToken
    ) {
        return ResponseEntity.ok(bookingService.releaseSeats(request.tripSeatIds(), holdToken)); 
    }
}
