package com.ralsei.controller;

import com.ralsei.dto.request.RouteStopRequest;
import com.ralsei.dto.response.RouteStopResponse;
import com.ralsei.dto.response.PagedResponse;
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
            @PathVariable Integer id,
            @Valid @RequestBody RouteStopRequest request) {
        RouteStopResponse response = routeStopService.updateRouteStop(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteStopResponse> getRouteStopById(@PathVariable Integer id) {
        RouteStopResponse response = routeStopService.getRouteStopById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<RouteStopResponse>> getAllRouteStops(
            @RequestParam(required = false) Integer routeId,
            @RequestParam(required = false) Integer stopPointId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<RouteStopResponse> response = routeStopService.getAllRouteStops(routeId, stopPointId, isActive,
                page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> deleteRouteStop(@PathVariable Integer id) {
        routeStopService.deleteRouteStop(id);
        return ResponseEntity.noContent().build();
    }
}
