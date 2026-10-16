package com.ralsei.customer.controller.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.customer.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.customer.model.enums.TripSeatStatus;
import com.ralsei.customer.repository.TripSeatRepository;

@RestController
@RequestMapping("/api/internal/trip-seats")
public class InternalTripSeatController {

    private final TripSeatRepository tripSeatRepository;

    public InternalTripSeatController(TripSeatRepository tripSeatRepository) {
        this.tripSeatRepository = tripSeatRepository;
    }

    @GetMapping("/count-by-trip/{tripId}")
    public ResponseEntity<Map<String, Object>> getSeatAvailabilityByTripId(@PathVariable Integer tripId) {
        List<TripSeatResponse> seatMap = tripSeatRepository.getSeatMap(tripId);
        long available = seatMap.stream().filter(s -> s.status() == TripSeatStatus.AVAILABLE).count();
        long sold = seatMap.stream().filter(s -> s.status() == TripSeatStatus.SOLD).count();
        long locked = seatMap.stream().filter(s -> s.status() == TripSeatStatus.LOCKED).count();

        Map<String, Object> result = new HashMap<>();
        result.put("tripId", tripId);
        result.put("totalSeats", seatMap.size());
        result.put("availableSeats", available);
        result.put("bookedSeats", sold);
        result.put("soldSeats", sold);
        result.put("lockedSeats", locked);
        return ResponseEntity.ok(result);
    }
}
