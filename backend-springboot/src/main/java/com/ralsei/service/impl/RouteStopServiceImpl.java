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
import com.ralsei.dto.request.route.RouteStopOrderUpdateRequest;

import java.util.ArrayList;
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

                boolean orderExists = route.getRouteStops().stream()
                                .anyMatch(rs -> rs.getStopOrder() == request.getStopOrder());
                if (orderExists) {
                        throw new IllegalArgumentException(
                                        "Stop order " + request.getStopOrder() + " already exists in this route.");
                }

                if (request.getKilometersFromStart() != null
                                && request.getKilometersFromStart().compareTo(route.getTotalKilometers()) > 0) {
                        throw new IllegalArgumentException(
                                        "Kilometers from start cannot be larger than route's total kilometers.");
                }
                if (request.getMinutesFromStart() > route.getTotalMinutes()) {
                        throw new IllegalArgumentException(
                                        "Minutes from start cannot be larger than route's total minutes.");
                }

                RouteStop routeStop = RouteStop.builder()
                                .route(route)
                                .coachStop(coachStop)
                                .stopOrder(request.getStopOrder())
                                .kilometersFromStart(request.getKilometersFromStart())
                                .minutesFromStart(request.getMinutesFromStart())
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

                boolean orderExists = route.getRouteStops().stream()
                                .anyMatch(rs -> rs.getRouteStopId() != id
                                                && rs.getStopOrder() == request.getStopOrder());
                if (orderExists) {
                        throw new IllegalArgumentException(
                                        "Stop order " + request.getStopOrder() + " already exists in this route.");
                }

                if (request.getKilometersFromStart() != null
                                && request.getKilometersFromStart().compareTo(route.getTotalKilometers()) > 0) {
                        throw new IllegalArgumentException(
                                        "Kilometers from start cannot be larger than route's total kilometers.");
                }
                if (request.getMinutesFromStart() > route.getTotalMinutes()) {
                        throw new IllegalArgumentException(
                                        "Minutes from start cannot be larger than route's total minutes.");
                }

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

        public PagedResponse<RouteStopResponse> getAllRouteStops(int routeId, int stopPointId, int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("stopOrder").ascending());
                Page<RouteStop> routeStopPage = routeStopRepository.searchRouteStops(
                                routeId,
                                stopPointId,
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

                routeStopRepository.delete(routeStop);
        }

        @Override
        @Transactional
        public List<RouteStopResponse> bulkUpdateOrders(List<RouteStopOrderUpdateRequest> requests) {
                if (requests == null || requests.isEmpty()) {
                        return new ArrayList<>();
                }

                // Fetch all RouteStops that need updating
                List<Integer> ids = requests.stream().map(RouteStopOrderUpdateRequest::getRouteStopId)
                                .collect(Collectors.toList());
                List<RouteStop> routeStops = routeStopRepository.findAllById(ids);

                if (routeStops.size() != requests.size()) {
                        throw new ResourceNotFoundException("One or more RouteStops not found.");
                }

                // Make sure all routeStops belong to the same route
                Integer routeId = routeStops.get(0).getRoute().getRouteId();
                boolean allSameRoute = routeStops.stream().allMatch(rs -> rs.getRoute().getRouteId() == routeId);
                if (!allSameRoute) {
                        throw new IllegalArgumentException("All RouteStops must belong to the same route.");
                }

                // Update orders in memory
                for (RouteStop rs : routeStops) {
                        for (RouteStopOrderUpdateRequest req : requests) {
                                if (rs.getRouteStopId() == req.getRouteStopId()) {
                                        rs.setStopOrder(req.getStopOrder());
                                        break;
                                }
                        }
                }

                // Save all
                List<RouteStop> saved = routeStopRepository.saveAll(routeStops);
                return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
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
                                .build();
        }
}
