package com.ralsei.service;

import java.util.List;

import com.ralsei.dto.request.CoachAndRouteStop.RouteStopRequest;
import com.ralsei.dto.request.route.RouteStopOrderUpdateRequest;
import com.ralsei.dto.response.CoachAndRouteStop.RouteStopResponse;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.model.RouteStop;
public interface RouteStopService {
    RouteStopResponse createRouteStop(RouteStopRequest request);

    RouteStopResponse updateRouteStop(int id, RouteStopRequest request);

    RouteStopResponse getRouteStopById(int id);

    PagedResponse<RouteStopResponse> getAllRouteStops(int routeId, int stopPointId, int page, int size);

    void deleteRouteStop(int id);

    List<RouteStopResponse> bulkUpdateOrders(List<RouteStopOrderUpdateRequest> requests);

    List<RouteStop> getStopsByTripId(Integer tripId);
}
