package com.ralsei.staff.controller.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.staff.model.Route;
import com.ralsei.staff.model.RouteStop;
import com.ralsei.staff.repository.RouteRepository;
import com.ralsei.staff.repository.RouteStopRepository;

@RestController
@RequestMapping("/api/internal/routes")
public class InternalRouteController {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    public InternalRouteController(RouteRepository routeRepository, RouteStopRepository routeStopRepository) {
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<?> getRouteById(@PathVariable Integer routeId) {
        return routeRepository.findById(routeId)
                .map(this::mapRoute)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{routeId}/stops")
    public ResponseEntity<?> getRouteStops(@PathVariable Integer routeId) {
        List<RouteStop> stops = routeStopRepository.findByRoute_RouteIdOrderByStopOrderAsc(routeId);
        List<Map<String, Object>> result = stops.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("routeStopId", s.getRouteStopId());
            map.put("routeId", s.getRoute() != null ? s.getRoute().getRouteId() : null);
            map.put("stopPointId", s.getCoachStop() != null ? s.getCoachStop().getStopPointId() : null);
            map.put("stopName", s.getCoachStop() != null ? s.getCoachStop().getStopPointName() : null);
            map.put("city", s.getCoachStop() != null ? s.getCoachStop().getCity() : null);
            map.put("stopOrder", s.getStopOrder());
            map.put("kilometersFromStart", s.getKilometersFromStart());
            map.put("minutesFromStart", s.getMinutesFromStart());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> mapRoute(Route r) {
        Map<String, Object> map = new HashMap<>();
        map.put("routeId", r.getRouteId());
        map.put("routeName", r.getRouteName());
        map.put("totalKilometers", r.getTotalKilometers());
        map.put("totalMinutes", r.getTotalMinutes());
        map.put("isActive", r.isActive());
        return map;
    }
}
