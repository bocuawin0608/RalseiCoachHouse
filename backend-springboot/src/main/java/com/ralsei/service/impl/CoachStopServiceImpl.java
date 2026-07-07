package com.ralsei.service.impl;

import com.ralsei.dto.request.CoachAndRouteStop.CoachStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.CoachStopResponse;
import com.ralsei.model.CoachStop;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.service.CoachStopService;
import com.ralsei.exception.ResourceNotFoundException;

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

    @Override
    @Transactional
    public CoachStopResponse createCoachStop(CoachStopRequest request) {
        if (coachStopRepository.existsByAddressIgnoreCaseAndCityIgnoreCase(request.getAddress().trim(),
                request.getCity().trim())) {
            throw new IllegalArgumentException("Đã tồn tại điểm dừng với địa chỉ và thành phố này.");
        }

        CoachStop coachStop = CoachStop.builder()
                .stopPointName(request.getStopPointName().trim())
                .address(request.getAddress().trim())
                .city(request.getCity().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(true)
                .build();
        CoachStop saved = coachStopRepository.save(Objects.requireNonNull(coachStop));
        return mapToResponse(saved);
    }

    @Override
    @Transactional
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
        }

        coachStop.setStopPointName(request.getStopPointName().trim());
        coachStop.setAddress(request.getAddress().trim());
        coachStop.setCity(request.getCity().trim());

        CoachStop updated = coachStopRepository.save(coachStop);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CoachStopResponse getCoachStopById(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachStop not found with ID: " + id));
        return mapToResponse(coachStop);
    }

    @Override
    @Transactional(readOnly = true)
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
    public void softDeleteCoachStop(int id) {
        CoachStop coachStop = coachStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachStop not found with ID: " + id));

        // Soft delete the coach stop
        coachStop.setActive(false);
        coachStopRepository.save(coachStop);
    }

    @Override
    @Transactional
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
                .build();
    }
}
