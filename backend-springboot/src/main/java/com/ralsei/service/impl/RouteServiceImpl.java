package com.ralsei.service.impl;

import com.ralsei.dto.request.RouteRequest;
import com.ralsei.dto.response.RouteResponse;
import com.ralsei.dto.response.RouteStopResponse;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.model.Route;
import com.ralsei.model.RouteStop;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.service.RouteService;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    @Override
    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        Route route = Route.builder()
                .routeName(request.getRouteName())
                .totalKilometers(request.getTotalKilometers())
                .totalMinutes(request.getTotalMinutes())
                .isActive(true)
                .routeStops(new ArrayList<>())
                .build();
            Route saved = routeRepository.save(Objects.requireNonNull(route));
            return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RouteResponse updateRoute(int id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with ID: " + id));

        route.setRouteName(request.getRouteName());
        route.setTotalKilometers(request.getTotalKilometers());
        route.setTotalMinutes(request.getTotalMinutes());

        Route updated = routeRepository.save(route);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(int id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with ID: " + id));
        return mapToResponse(route);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RouteResponse> getAllRoutes(String search, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("routeId").descending());
        Page<Route> routePage = routeRepository.searchRoutes(search, isActive, pageable);

        List<RouteResponse> content = routePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                routePage.getNumber(),
                routePage.getSize(),
                routePage.getTotalElements(),
                routePage.getTotalPages(),
                routePage.isLast());
    }

    @Override
    @Transactional
    public void deleteRoute(int id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with ID: " + id));

        // Soft delete the route itself
        route.setActive(false);
        routeRepository.save(route);

        // Cascade soft delete to associated route stops
        List<RouteStop> routeStops = routeStopRepository.findByRoute_RouteIdOrderByStopOrderAsc(id);
        for (RouteStop routeStop : routeStops) {
            routeStop.setActive(false);
            routeStopRepository.save(routeStop);
        }
    }

    private RouteResponse mapToResponse(Route route) {
        if (route == null) {
            return null;
        }
        List<RouteStopResponse> stopResponses = null;
        if (route.getRouteStops() != null) {
            stopResponses = route.getRouteStops().stream()
                    .map(this::mapStopToResponse)
                    .collect(Collectors.toList());
        }
        return RouteResponse.builder()
                .routeId(route.getRouteId())
                .routeName(route.getRouteName())
                .totalKilometers(route.getTotalKilometers())
                .totalMinutes(route.getTotalMinutes())
                .isActive(route.isActive())
                .createdAt(route.getCreatedAt())
                .createdBy(route.getCreatedBy())
                .updatedAt(route.getUpdatedAt())
                .updatedBy(route.getUpdatedBy())
                .routeStops(stopResponses)
                .build();
    }

    private RouteStopResponse mapStopToResponse(RouteStop rs) {
        if (rs == null) {
            return null;
        }
        return RouteStopResponse.builder()
                .routeStopId(rs.getRouteStopId())
                .routeId(rs.getRoute() != null ? rs.getRoute().getRouteId() : 0)
                .routeName(rs.getRoute() != null ? rs.getRoute().getRouteName() : null)
                .stopPointId(rs.getCoachStop() != null ? rs.getCoachStop().getStopPointId() : 0)
                .stopPointName(rs.getCoachStop() != null ? rs.getCoachStop().getStopPointName() : null)
                .address(rs.getCoachStop() != null ? rs.getCoachStop().getAddress() : null)
                .stopOrder(rs.getStopOrder())
                .kilometersFromStart(rs.getKilometersFromStart())
                .minutesFromStart(rs.getMinutesFromStart())
                .isActive(rs.isActive())
                .createdAt(rs.getCreatedAt())
                .createdBy(rs.getCreatedBy())
                .updatedAt(rs.getUpdatedAt())
                .updatedBy(rs.getUpdatedBy())
                .build();
    }
}
