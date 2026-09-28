package com.ralsei.staff.service;

import com.ralsei.staff.dto.request.goong.CalculateRouteDistancesRequest;
import com.ralsei.staff.dto.request.goong.DistanceTimeRequest;
import com.ralsei.staff.dto.response.goong.CalculateRouteDistancesResponse;
import com.ralsei.staff.dto.response.goong.DistanceTimeResponse;
import com.ralsei.staff.dto.response.goong.GeocodeResponse;
import com.ralsei.staff.model.RouteStop;
import java.util.List;

/**
 * Provides the business service contract for goong.
 */
public interface GoongService {
    Object autocomplete(String input);
    DistanceTimeResponse getDistanceAndTime(DistanceTimeRequest request);
    CalculateRouteDistancesResponse calculateRouteDistances(CalculateRouteDistancesRequest request);
    void calculateAndSetRouteStopsDistances(List<RouteStop> sortedStops);
    GeocodeResponse geocode(String address);
}
