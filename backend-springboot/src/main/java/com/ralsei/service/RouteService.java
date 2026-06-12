package com.ralsei.service;

import com.ralsei.dto.request.CoachAndRouteStop.RouteRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteResponse;

public interface RouteService {
    RouteResponse createRoute(RouteRequest request);

    RouteResponse updateRoute(int id, RouteRequest request);

    RouteResponse getRouteById(int id);

    PagedResponse<RouteResponse> getAllRoutes(String search, Boolean isActive, int page, int size);

    void softDeleteRoute(int id);

    void restoreRoute(int id);
}
