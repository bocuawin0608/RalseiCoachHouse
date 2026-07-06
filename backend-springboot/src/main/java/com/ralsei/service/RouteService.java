package com.ralsei.service;

import java.util.List;

import com.ralsei.dto.request.CoachAndRouteStop.RouteRequest;
import com.ralsei.dto.request.CoachAndRouteStop.RouteWithStopsRequest;
import com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO;
import com.ralsei.dto.response.CoachAndRouteStop.RouteResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteWithStopsResponse;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.projection.route.RouteLocationDropdownProjection;

public interface RouteService {
    RouteResponse createRoute(RouteRequest request);

    RouteWithStopsResponse createRouteWithStops(RouteWithStopsRequest request);

    RouteResponse updateRoute(int id, RouteRequest request);

    RouteResponse getRouteById(int id);

    PagedResponse<RouteResponse> getAllRoutes(String search, Boolean isActive, int page, int size);

    void softDeleteRoute(int id);

    void restoreRoute(int id);

    List<RouteDropdownDTO> findRoutesForDropdown();

    /** Returns active route locations for the public customer search form. */
    List<RouteLocationDropdownProjection> findRouteLocationsForCustomerDropdown();
}
