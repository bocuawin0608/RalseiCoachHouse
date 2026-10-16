package com.ralsei.staff.controller.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.staff.model.Trip;
import com.ralsei.staff.repository.TripRepository;

@RestController
@RequestMapping("/api/internal/trips")
public class InternalTripController {

    private final TripRepository tripRepository;

    public InternalTripController(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getTripById(@PathVariable Integer tripId) {
        return tripRepository.findById(tripId)
                .map(this::mapTrip)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Map<String, Object>>> getTripsBatch(@RequestBody List<Integer> tripIds) {
        if (tripIds == null || tripIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Trip> trips = tripRepository.findAllById(tripIds);
        List<Map<String, Object>> result = trips.stream().map(this::mapTrip).toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> mapTrip(Trip t) {
        Map<String, Object> map = new HashMap<>();
        map.put("tripId", t.getTripId());
        map.put("routeId", t.getRouteId());
        map.put("coachId", t.getCoachId());
        map.put("departureTime", t.getDepartureTime());
        map.put("status", t.getStatus());
        map.put("driverId", t.getDriverId());
        map.put("attendantId", t.getAttendantId());
        return map;
    }
}
