package com.ralsei.staff.service;

import com.ralsei.staff.dto.request.CoachAndRouteStop.CoachStopRequest;
import com.ralsei.staff.dto.response.PagedResponse;
import com.ralsei.staff.dto.response.CoachAndRouteStop.CoachStopResponse;

/**
 * Provides the business service contract for coach stop.
 */
public interface CoachStopService {
    CoachStopResponse createCoachStop(CoachStopRequest request);

    CoachStopResponse updateCoachStop(int id, CoachStopRequest request);

    CoachStopResponse getCoachStopById(int id);

    PagedResponse<CoachStopResponse> getAllCoachStops(String search, Boolean isActive, int page, int size);

    void softDeleteCoachStop(int id);

    void restoreCoachStop(int id);
}
