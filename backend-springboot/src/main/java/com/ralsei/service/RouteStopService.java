package com.ralsei.service;

import com.ralsei.dto.request.CoachAndRouteStop.RouteStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteStopResponse;

public interface RouteStopService {
    RouteStopResponse createRouteStop(RouteStopRequest request);

    RouteStopResponse updateRouteStop(int id, RouteStopRequest request);

    RouteStopResponse getRouteStopById(int id);

    PagedResponse<RouteStopResponse> getAllRouteStops(int routeId, int stopPointId, Boolean isActive, int page,
            int size);

    void deleteRouteStop(int id);
}
