package com.ralsei.service;

import com.ralsei.dto.request.CoachStopRequest;
import com.ralsei.dto.response.CoachStopResponse;
import com.ralsei.dto.response.PagedResponse;

public interface CoachStopService {
    CoachStopResponse createCoachStop(CoachStopRequest request);
    CoachStopResponse updateCoachStop(int id, CoachStopRequest request);
    CoachStopResponse getCoachStopById(int id);
    PagedResponse<CoachStopResponse> getAllCoachStops(String search, Boolean isActive, int page, int size);
    void deleteCoachStop(int id);
}
