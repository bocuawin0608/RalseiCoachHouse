package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.coach.CoachCreateRequest;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.response.coach.CoachResponse;

public interface CoachService {
    Page<CoachResponse> filterCoaches(CoachFilterRequest filterRequest, Pageable pageable);

    Integer createCoach(CoachCreateRequest request);
}
