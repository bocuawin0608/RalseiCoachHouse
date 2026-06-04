package com.ralsei.service.impl;

import com.ralsei.dto.request.CoachStopRequest;
import com.ralsei.dto.response.CoachStopResponse;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.model.CoachStop;
import com.ralsei.model.RouteStop;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.service.CoachStopService;
import jakarta.persistence.EntityNotFoundException;

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
public class CoachStopServiceImpl implements CoachStopService {

    private final CoachStopRepository coachStopRepository;
    private final RouteStopRepository routeStopRepository;

    @Override
    @Transactional
    public CoachStopResponse createCoachStop(CoachStopRequest request) {
        CoachStop coachStop = CoachStop.builder()
                .stopPointName(request.getStopPointName())
                .address(request.getAddress())
                .isActive(true)
                .build();
        CoachStop saved = coachStopRepository.save(Objects.requireNonNull(coachStop));
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CoachStopResponse updateCoachStop(int id, CoachStopRequest request) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CoachStop not found with ID: " + id));

        coachStop.setStopPointName(request.getStopPointName());
        coachStop.setAddress(request.getAddress());

        CoachStop updated = coachStopRepository.save(coachStop);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CoachStopResponse getCoachStopById(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CoachStop not found with ID: " + id));
        return mapToResponse(coachStop);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CoachStopResponse> getAllCoachStops(String search, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("stopPointId").descending());
        Page<CoachStop> coachStopPage = coachStopRepository.searchCoachStops(search, isActive, pageable);

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
    public void deleteCoachStop(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CoachStop not found with ID: " + id));

        // Soft delete the coach stop
        coachStop.setActive(false);
        coachStopRepository.save(coachStop);

        // Cascade soft delete to associated coach stops
        List<RouteStop> routeStops = routeStopRepository.findByCoachStop_StopPointId(id);
        for (RouteStop routeStop : routeStops) {
            routeStop.setActive(false);
            routeStopRepository.save(routeStop);
        }
    }

    @Override
    @Transactional
    public void restoreCoachStop(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CoachStop not found with ID: " + id));

        // Restore the coachStop itself
        coachStop.setActive(true);
        coachStopRepository.save(coachStop);

        // Cascade restore to associated route stops
        List<RouteStop> routeStops = routeStopRepository.findByCoachStop_StopPointId(id);
        for (RouteStop routeStop : routeStops) {
            routeStop.setActive(true);
            routeStopRepository.save(routeStop);
        }
    }

    private CoachStopResponse mapToResponse(CoachStop coachStop) {
        if (coachStop == null) {
            return null;
        }
        return CoachStopResponse.builder()
                .stopPointId(coachStop.getStopPointId())
                .stopPointName(coachStop.getStopPointName())
                .address(coachStop.getAddress())
                .isActive(coachStop.isActive())
                .createdAt(coachStop.getCreatedAt())
                .createdBy(coachStop.getCreatedBy())
                .updatedAt(coachStop.getUpdatedAt())
                .updatedBy(coachStop.getUpdatedBy())
                .build();
    }
}
