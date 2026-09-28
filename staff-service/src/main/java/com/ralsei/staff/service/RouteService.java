package com.ralsei.staff.service;

import java.util.List;

import com.ralsei.staff.dto.request.CoachAndRouteStop.RouteRequest;
import com.ralsei.staff.dto.request.CoachAndRouteStop.RouteWithStopsRequest;
import com.ralsei.staff.dto.response.CoachAndRouteStop.RouteDropdownDTO;
import com.ralsei.staff.dto.response.CoachAndRouteStop.RouteResponse;
import com.ralsei.staff.dto.response.CoachAndRouteStop.RouteWithStopsResponse;
import com.ralsei.staff.dto.response.PagedResponse;
import com.ralsei.staff.dto.projection.route.RouteLocationDropdownProjection;

/**
 * Provides the business service contract for route.
 */
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
