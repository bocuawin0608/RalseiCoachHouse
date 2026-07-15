package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.request.CoachAndRouteStop.RouteStopRequest;
import com.ralsei.dto.request.route.RouteStopOrderUpdateRequest;
import com.ralsei.dto.response.CoachAndRouteStop.RouteStopResponse;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CoachStop;
import com.ralsei.model.Route;
import com.ralsei.model.RouteStop;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.service.RouteStopService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Provides the route stop service impl component for the application.
 */
public class RouteStopServiceImpl implements RouteStopService {

        private final RouteStopRepository routeStopRepository;
        private final RouteRepository routeRepository;
        private final CoachStopRepository coachStopRepository;

        @Override
        @Transactional
        /**
         * Creates the route stop.
         *
         * @param request the value supplied for this operation
         *
         * @return the created route stop
         */
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
                        List<RouteStop> toShift = route.getRouteStops().stream()
                                        .filter(rs -> rs.getStopOrder() >= request.getStopOrder())
                                        .sorted((a, b) -> Integer.compare(b.getStopOrder(), a.getStopOrder()))
                                        .collect(Collectors.toList());
                        for (RouteStop rs : toShift) {
                                rs.setStopOrder(rs.getStopOrder() + 1);
                        }
                        routeStopRepository.saveAll(toShift);
                        routeStopRepository.flush();
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
                boolean isBothZero = (request.getMinutesFromStart() == 0 && request.getKilometersFromStart() != null
                                && request.getKilometersFromStart().compareTo(BigDecimal.ZERO) == 0);
                boolean isBothPositive = (request.getMinutesFromStart() > 0 && request.getKilometersFromStart() != null
                                && request.getKilometersFromStart().compareTo(BigDecimal.ZERO) > 0);
                if (!isBothZero && !isBothPositive) {
                        throw new IllegalArgumentException(
                                        "Khoảng cách và thời gian phải cùng bằng 0 hoặc cùng lớn hơn 0.");
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

        // deprecated
        @Override
        @Transactional
        /**
         * Updates the route stop.
         *
         * @param id the value supplied for this operation
         * @param request the value supplied for this operation
         *
         * @return the updated route stop
         */
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
                boolean isBothZero = (request.getMinutesFromStart() == 0 && request.getKilometersFromStart() != null
                                && request.getKilometersFromStart().compareTo(BigDecimal.ZERO) == 0);
                boolean isBothPositive = (request.getMinutesFromStart() > 0 && request.getKilometersFromStart() != null
                                && request.getKilometersFromStart().compareTo(BigDecimal.ZERO) > 0);
                if (!isBothZero && !isBothPositive) {
                        throw new IllegalArgumentException(
                                        "Khoảng cách và thời gian phải cùng bằng 0 hoặc cùng lớn hơn 0.");
                }

                routeStop.setRoute(route);
                routeStop.setCoachStop(coachStop);
                routeStop.setStopOrder(request.getStopOrder());
                routeStop.setKilometersFromStart(request.getKilometersFromStart());
                routeStop.setMinutesFromStart(request.getMinutesFromStart());

                RouteStop updated = routeStopRepository.save(routeStop);
                routeStopRepository.flush();
                syncRouteTotals(route.getRouteId());
                return mapToResponse(updated);
        }

        @Override
        @Transactional(readOnly = true)
        /**
         * Returns the route stop by id.
         *
         * @param id the value supplied for this operation
         *
         * @return the route stop by id
         */
        public RouteStopResponse getRouteStopById(int id) {
                RouteStop routeStop = routeStopRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("RouteStop not found with ID: " + id));
                return mapToResponse(routeStop);
        }

        /**
         * Returns the all route stops.
         *
         * @param routeId the value supplied for this operation
         * @param stopPointId the value supplied for this operation
         * @param page the value supplied for this operation
         * @param size the value supplied for this operation
         *
         * @return the all route stops
         */
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
        /**
         * Deletes the route stop.
         *
         * @param id the value supplied for this operation
         */
        public void deleteRouteStop(int id) {
                RouteStop routeStop = routeStopRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("RouteStop not found with ID: " + id));

                int routeId = routeStop.getRoute().getRouteId();

                List<RouteStop> currentStops = routeStopRepository.findByRoute_RouteIdOrderByStopOrderAsc(routeId);
                if (currentStops.size() <= 2) {
                        throw new IllegalArgumentException("Cannot delete RouteStop. A route must have at least 2 stops.");
                }

                routeStopRepository.delete(routeStop);
                routeStopRepository.flush();

                // Reset stop orders to ascending sequence (1, 2, 3, ...)
                List<RouteStop> remainingStops = routeStopRepository
                                .findByRoute_RouteIdOrderByStopOrderAsc(routeId);

                if (routeStop.getStopOrder() == 1 && !remainingStops.isEmpty()) {
                        RouteStop newFirstStop = remainingStops.get(0);
                        BigDecimal subtractKm = newFirstStop.getKilometersFromStart();
                        int subtractMinutes = newFirstStop.getMinutesFromStart();

                        for (RouteStop rs : remainingStops) {
                                BigDecimal currentKm = rs.getKilometersFromStart();
                                if (currentKm != null && subtractKm != null) {
                                        BigDecimal newKm = currentKm.subtract(subtractKm);
                                        rs.setKilometersFromStart(
                                                        newKm.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newKm);
                                }
                                int newMinutes = rs.getMinutesFromStart() - subtractMinutes;
                                rs.setMinutesFromStart(Math.max(0, newMinutes));
                        }
                }

                int order = 1;
                for (RouteStop rs : remainingStops) {
                        rs.setStopOrder(order++);
                }

                routeStopRepository.saveAll(remainingStops);
                routeStopRepository.flush();
                syncRouteTotals(routeId);
        }

        @Override
        @Transactional
        /**
         * Executes the bulk update orders operation.
         *
         * @param requests the value supplied for this operation
         *
         * @return the operation result
         */
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
                                .city(rs.getCoachStop() != null ? rs.getCoachStop().getCity() : null)
                                .stopOrder(rs.getStopOrder())
                                .kilometersFromStart(rs.getKilometersFromStart())
                                .minutesFromStart(rs.getMinutesFromStart())
                                .build();
        }

        @Transactional(readOnly = true)
        @Override
        /**
         * Returns the stops by trip id.
         *
         * @param tripId the value supplied for this operation
         *
         * @return the stops by trip id
         */
        public List<RouteStop> getStopsByTripId(Integer tripId) {
                return routeStopRepository.findByTripIdWithCoachStop(tripId);
        }

        // sync route total kilometers and total minutes corresponding to the last stops
        private void syncRouteTotals(int routeId) {
                List<RouteStop> stops = routeStopRepository.findByRoute_RouteIdOrderByStopOrderAsc(routeId);
                if (stops.isEmpty()) {
                        routeRepository.updateRouteTotals(routeId, BigDecimal.ZERO, 0);
                } else {
                        RouteStop lastStop = stops.get(stops.size() - 1);
                        BigDecimal km = lastStop.getKilometersFromStart() != null ? lastStop.getKilometersFromStart()
                                        : BigDecimal.ZERO;
                        int mins = lastStop.getMinutesFromStart();
                        routeRepository.updateRouteTotals(routeId, km, mins);
                }
        }
}
