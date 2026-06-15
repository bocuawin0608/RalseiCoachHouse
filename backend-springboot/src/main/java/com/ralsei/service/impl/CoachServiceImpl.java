package com.ralsei.service.impl;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.request.coach.CoachCreateRequest;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Coach;
import com.ralsei.model.CoachType;
import com.ralsei.repository.CoachRepository;
import com.ralsei.repository.CoachTypeRepository;
import com.ralsei.service.CoachService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachTypeRepository coachTypeRepo;
    private final CoachRepository coachRepo;

    @Transactional(readOnly = true)
    @Override
    public Page<CoachResponse> filterCoaches(CoachFilterRequest filterRequest, Pageable pageable) {
        return coachRepo.searchCoaches(filterRequest, pageable);
    }

    @Transactional
    @Override
    public Integer createCoach(CoachCreateRequest request) {
        CoachType coachType = coachTypeRepo.findByCoachTypeIdAndIsActiveTrue(request.coachTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Loại xe không tồn tại hoặc không hợp lệ!"));

        if(coachRepo.existsByLicensePlateIgnoreCase(request.licensePlate())) {
            throw new IllegalArgumentException("Biển số xe này đã tồn tại trong hệ thống!");
        }

        Coach newCoach = Coach.builder()
            .coachType(coachType)
            .licensePlate(request.licensePlate())
            .manufacturer(request.manufacturer())
            .year(request.year())
            .status(request.status())
            .seats(new ArrayList<>())
            .build();

        // Seat
            
        return null;
    }
}
