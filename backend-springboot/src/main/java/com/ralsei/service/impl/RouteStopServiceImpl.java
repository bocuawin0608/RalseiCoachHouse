package com.ralsei.service.impl;

import com.ralsei.dto.request.CoachAndRouteStop.RouteStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteStopResponse;
import com.ralsei.model.CoachStop;
import com.ralsei.model.Route;
import com.ralsei.model.RouteStop;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.service.RouteStopService;
import com.ralsei.exception.ResourceNotFoundException;

import java.util.Objects;
import java.util.List;
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
public class RouteStopServiceImpl implements RouteStopService {

        private final RouteStopRepository routeStopRepository;
        private final RouteRepository routeRepository;
        private final CoachStopRepository coachStopRepository;

        @Override
        @Transactional
        public RouteStopResponse createRouteStop(RouteStopRequest request) {
                Route route = routeRepository.findById(Objects.requireNonNull(request.getRouteId()))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Route not found with ID: " + request.getRouteId()));

                CoachStop coachStop = coachStopRepository.findById(Objects.requireNonNull(request.getStopPointId()))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "CoachStop not found with ID: " + request.getStopPointId()));

                RouteStop routeStop = RouteStop.builder()
                                .route(route)
                                .coachStop(coachStop)
                                .stopOrder(request.getStopOrder())
                                .kilometersFromStart(request.getKilometersFromStart())
                                .minutesFromStart(request.getMinutesFromStart())
                                .isActive(true)
                                .build();

                RouteStop saved = routeStopRepository.save(Objects.requireNonNull(routeStop));
                return mapToResponse(saved);
        }

        @Override
        @Transactional
        public RouteStopResponse updateRouteStop(int id, RouteStopRequest request) {
                RouteStop routeStop = routeStopRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("RouteStop not found with ID: " + id));

                Route route = routeRepository.findById(Objects.requireNonNull(request.getRouteId()))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Route not found with ID: " + request.getRouteId()));

                CoachStop coachStop = coachStopRepository.findById(Objects.requireNonNull(request.getStopPointId()))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "CoachStop not found with ID: " + request.getStopPointId()));

                routeStop.setRoute(route);
                routeStop.setCoachStop(coachStop);
                routeStop.setStopOrder(request.getStopOrder());
                routeStop.setKilometersFromStart(request.getKilometersFromStart());
                routeStop.setMinutesFromStart(request.getMinutesFromStart());

                RouteStop updated = routeStopRepository.save(routeStop);
                return mapToResponse(updated);
        }

        @Override
        @Transactional(readOnly = true)
        public RouteStopResponse getRouteStopById(int id) {
                RouteStop routeStop = routeStopRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("RouteStop not found with ID: " + id));
                return mapToResponse(routeStop);
        }

        @Override
        @Transactional(readOnly = true)
        public PagedResponse<RouteStopResponse> getAllRouteStops(int routeId, int stopPointId, Boolean isActive,
                        int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("stopOrder").ascending());
                Page<RouteStop> routeStopPage = routeStopRepository.searchRouteStops(
                                routeId, 
                                stopPointId, 
                                isActive,
                                pageable);

                List<RouteStopResponse> content = routeStopPage.getContent().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return new PagedResponse<>(
                                content,
                                routeStopPage.getNumber(),
                                routeStopPage.getSize(),
                                routeStopPage.getTotalElements(),
                                routeStopPage.getTotalPages(),
                                routeStopPage.isLast());
        }

        @Override
        @Transactional
        public void deleteRouteStop(int id) {
                RouteStop routeStop = routeStopRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("RouteStop not found with ID: " + id));

                routeStop.setActive(false);
                routeStopRepository.save(routeStop);
        }

        private RouteStopResponse mapToResponse(RouteStop rs) {
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
                                .build();
        }
}
