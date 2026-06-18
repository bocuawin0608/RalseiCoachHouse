package com.ralsei.service;

import com.ralsei.dto.request.CoachAndRouteStop.CoachStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.CoachStopResponse;

public interface CoachStopService {
    CoachStopResponse createCoachStop(CoachStopRequest request);

    CoachStopResponse updateCoachStop(int id, CoachStopRequest request);

    CoachStopResponse getCoachStopById(int id);

    PagedResponse<CoachStopResponse> getAllCoachStops(String search, Boolean isActive, int page, int size);

    void softDeleteCoachStop(int id);

    void restoreCoachStop(int id);
}
