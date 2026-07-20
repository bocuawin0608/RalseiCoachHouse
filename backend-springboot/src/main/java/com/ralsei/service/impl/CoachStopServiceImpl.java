package com.ralsei.service.impl;

import com.ralsei.dto.request.CoachAndRouteStop.CoachStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.CoachStopResponse;
import com.ralsei.model.CoachStop;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.service.CoachStopService;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.service.GoongService;
import com.ralsei.dto.response.goong.GeocodeResponse;
import com.ralsei.repository.RouteRepository;
import com.ralsei.model.RouteStop;
import com.ralsei.model.Route;

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
/**
 * Provides the coach stop service impl component for the application.
 */
public class CoachStopServiceImpl implements CoachStopService {

    private final CoachStopRepository coachStopRepository;
    private final GoongService goongService;
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    /**
     * Creates the coach stop.
     *
     * @param request the value supplied for this operation
     *
     * @return the created coach stop
     */
    public CoachStopResponse createCoachStop(CoachStopRequest request) {
        if (coachStopRepository.existsByAddressIgnoreCaseAndCityIgnoreCase(request.getAddress().trim(),
                request.getCity().trim())) {
            throw new IllegalArgumentException("Đã tồn tại điểm dừng với địa chỉ và thành phố này.");
        }

        GeocodeResponse geo = goongService.geocode(request.getAddress().trim() + ", " + request.getCity().trim());

        CoachStop coachStop = CoachStop.builder()
                .stopPointName(request.getStopPointName().trim())
                .address(request.getAddress().trim())
                .city(request.getCity().trim())
                .latitude(geo.getLatitude())
                .longitude(geo.getLongitude())
                .isActive(true)
                .build();
        CoachStop saved = coachStopRepository.save(Objects.requireNonNull(coachStop));

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    /**
     * Updates the coach stop.
     *
     * @param id the value supplied for this operation
     * @param request the value supplied for this operation
     *
     * @return the updated coach stop
     */
    public CoachStopResponse updateCoachStop(int id, CoachStopRequest request) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachStop not found with ID: " + id));

        boolean addressChanged = !coachStop.getAddress().equalsIgnoreCase(request.getAddress().trim());
        boolean cityChanged = !coachStop.getCity().equalsIgnoreCase(request.getCity().trim());

        if (addressChanged || cityChanged) {
            if (coachStopRepository.existsByAddressIgnoreCaseAndCityIgnoreCase(request.getAddress().trim(),
                    request.getCity().trim())) {
                throw new IllegalArgumentException("Đã tồn tại điểm dừng với địa chỉ và thành phố này.");
            }
            
            GeocodeResponse geo = goongService.geocode(request.getAddress().trim() + ", " + request.getCity().trim());
            coachStop.setLatitude(geo.getLatitude());
            coachStop.setLongitude(geo.getLongitude());
        }

        coachStop.setStopPointName(request.getStopPointName().trim());
        coachStop.setAddress(request.getAddress().trim());
        coachStop.setCity(request.getCity().trim());

        CoachStop updated = coachStopRepository.save(coachStop);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns the coach stop by id.
     *
     * @param id the value supplied for this operation
     *
     * @return the coach stop by id
     */
    public CoachStopResponse getCoachStopById(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachStop not found with ID: " + id));
        return mapToResponse(coachStop);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns the all coach stops.
     *
     * @param search the value supplied for this operation
     * @param isActive the value supplied for this operation
     * @param page the value supplied for this operation
     * @param size the value supplied for this operation
     *
     * @return the all coach stops
     */
    public PagedResponse<CoachStopResponse> getAllCoachStops(String search, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("stopPointId").descending());
        String safeSearch = search != null ? search.trim() : null;
        Page<CoachStop> coachStopPage = coachStopRepository.searchCoachStops(safeSearch, isActive, pageable);

        List<CoachStopResponse> content = coachStopPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                coachStopPage.getNumber(),
                coachStopPage.getSize(),
                coachStopPage.getTotalElements(),
                coachStopPage.getTotalPages(),
                coachStopPage.isLast());
    }

    @Override
    @Transactional
    /**
     * Executes the soft delete coach stop operation.
     *
     * @param id the value supplied for this operation
     */
    public void softDeleteCoachStop(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachStop not found with ID: " + id));

        // Soft delete the coach stop
        coachStop.setActive(false);
        coachStopRepository.save(coachStop);

        // Check if this coach stop is mapped in a route that consists of only 2 items. If so, disable the route.
        if (coachStop.getRouteStops() != null) {
            for (RouteStop rs : coachStop.getRouteStops()) {
                Route route = rs.getRoute();
                if (route != null && route.isActive() && route.getRouteStops().size() == 2) {
                    route.setActive(false);
                    routeRepository.save(route);
                }
            }
        }
    }

    @Override
    @Transactional
    /**
     * Executes the restore coach stop operation.
     *
     * @param id the value supplied for this operation
     */
    public void restoreCoachStop(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachStop not found with ID: " + id));

        // Restore the coachStop itself
        coachStop.setActive(true);
        coachStopRepository.save(coachStop);

    }

    private CoachStopResponse mapToResponse(CoachStop coachStop) {
        if (coachStop == null) {
            return null;
        }
        return CoachStopResponse.builder()
                .stopPointId(coachStop.getStopPointId())
                .stopPointName(coachStop.getStopPointName())
                .address(coachStop.getAddress())
                .city(coachStop.getCity())
                .isActive(coachStop.isActive())
                .latitude(coachStop.getLatitude())
                .longitude(coachStop.getLongitude())
                .build();
    }
}
