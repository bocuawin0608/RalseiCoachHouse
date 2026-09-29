package com.ralsei.customer.service;

import java.util.List;

import com.ralsei.customer.dto.request.CoachAndRouteStop.RouteStopRequest;
import com.ralsei.customer.dto.request.route.RouteStopOrderUpdateRequest;
import com.ralsei.customer.dto.response.CoachAndRouteStop.RouteStopResponse;
import com.ralsei.customer.dto.response.PagedResponse;
import com.ralsei.customer.model.RouteStop;

/**
 * Provides the business service contract for route stop.
 */
public interface RouteStopService {
    RouteStopResponse createRouteStop(RouteStopRequest request);

    RouteStopResponse updateRouteStop(int id, RouteStopRequest request);

    RouteStopResponse getRouteStopById(int id);

    PagedResponse<RouteStopResponse> getAllRouteStops(int routeId, int stopPointId, int page, int size);

    void deleteRouteStop(int id);

    List<RouteStopResponse> bulkUpdateOrders(List<RouteStopOrderUpdateRequest> requests);

    List<RouteStop> getStopsByTripId(Integer tripId);
}
