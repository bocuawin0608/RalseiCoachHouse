package com.ralsei.driver.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.driver.dto.request.coach.CoachCreateRequest;
import com.ralsei.driver.dto.request.coach.CoachFilterRequest;
import com.ralsei.driver.dto.request.coach.CoachReactivateRequest;
import com.ralsei.driver.dto.request.coach.CoachReportMaintenanceRequest;
import com.ralsei.driver.dto.request.coach.CoachRetireRequest;
import com.ralsei.driver.dto.request.coach.CoachUpdateInfoRequest;
import com.ralsei.driver.dto.request.coach.CoachUpdateSeatsRequest;
import com.ralsei.driver.dto.response.coach.CoachDetailResponse;
import com.ralsei.driver.dto.response.coach.CoachResponse;
import com.ralsei.driver.dto.response.coach.CoachStatusChangeCheckResponse;
import com.ralsei.driver.dto.response.coach.CoachStatusLogResponse;
import com.ralsei.driver.model.CoachStatus;

/**
 * Provides the business service contract for coach.
 */
public interface CoachService {
    Page<CoachResponse> filterCoaches(CoachFilterRequest filterRequest, Pageable pageable);
    Integer createCoach(CoachCreateRequest request);
    boolean updateCoachInfo(Integer id, CoachUpdateInfoRequest request);
    CoachDetailResponse getCoachDetail(Integer id);
    void reportMaintenance(Integer id, CoachReportMaintenanceRequest request);
    void reactivate(Integer id, CoachReactivateRequest request);
    void retire(Integer id, CoachRetireRequest request);
    CoachStatusChangeCheckResponse getStatusChangeCheck(Integer id, CoachStatus target);
    Page<CoachStatusLogResponse> getStatusLogs(Integer id, Pageable pageable);
    void updateCoachSeats(Integer id, CoachUpdateSeatsRequest request);
}
