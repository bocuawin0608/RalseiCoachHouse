package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.coachtype.CoachTypeCreateRequest;
import com.ralsei.dto.request.coachtype.CoachTypeFilterRequest;
import com.ralsei.dto.response.coachtype.CoachTypeDetailResponse;
import com.ralsei.dto.response.coachtype.CoachTypeResponse;

public interface CoachTypeService {
    Page<CoachTypeResponse> filterCoachTypes(CoachTypeFilterRequest filterRequest, Pageable pageable);
    Integer createCoachType(CoachTypeCreateRequest request);
    CoachTypeDetailResponse getCoachTypeDetail(Integer id);
}
