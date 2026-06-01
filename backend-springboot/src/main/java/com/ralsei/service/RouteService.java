package com.ralsei.service;

import com.ralsei.dto.request.RouteRequest;
import com.ralsei.dto.response.RouteResponse;
import com.ralsei.dto.response.PagedResponse;

public interface RouteService {
    RouteResponse createRoute(RouteRequest request);
    RouteResponse updateRoute(int id, RouteRequest request);
    RouteResponse getRouteById(int id);
    PagedResponse<RouteResponse> getAllRoutes(String search, Boolean isActive, int page, int size);
    void deleteRoute(int id);
}
