package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.coach.CoachCreateRequest;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.request.coach.CoachUpdateInfoRequest;
import com.ralsei.dto.response.coach.CoachEditFormResponse;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.dto.response.coach.CoachViewDetailResponse;

public interface CoachService {
    Page<CoachResponse> filterCoaches(CoachFilterRequest filterRequest, Pageable pageable);
    Integer createCoach(CoachCreateRequest request);
    boolean updateCoachInfo(Integer id, CoachUpdateInfoRequest request);
    CoachViewDetailResponse getCoachDetailForView(Integer id);
    CoachEditFormResponse getCoachDetailForEdit(Integer id);
}
