package com.ralsei.controller;

import com.ralsei.dto.request.CoachAndRouteStop.RouteStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteStopResponse;
import com.ralsei.service.RouteStopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/route-stops")
@RequiredArgsConstructor
public class RouteStopController {

    private final RouteStopService routeStopService;

    @PostMapping
    public ResponseEntity<RouteStopResponse> createRouteStop(@Valid @RequestBody RouteStopRequest request) {
        RouteStopResponse response = routeStopService.createRouteStop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteStopResponse> updateRouteStop(
            @PathVariable int id,
            @Valid @RequestBody RouteStopRequest request) {
        RouteStopResponse response = routeStopService.updateRouteStop(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteStopResponse> getRouteStopById(@PathVariable int id) {
        RouteStopResponse response = routeStopService.getRouteStopById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<RouteStopResponse>> getAllRouteStops(
            @RequestParam(required = false, defaultValue = "0") int routeId,
            @RequestParam(required = false, defaultValue = "0") int stopPointId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<RouteStopResponse> response = routeStopService.getAllRouteStops(routeId, stopPointId,
                page, size);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRouteStop(@PathVariable int id) {
        routeStopService.deleteRouteStop(id);
        return ResponseEntity.noContent().build();
    }
}
