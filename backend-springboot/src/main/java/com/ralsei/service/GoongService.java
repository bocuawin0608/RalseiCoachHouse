package com.ralsei.service;

import com.ralsei.dto.request.goong.CalculateRouteDistancesRequest;
import com.ralsei.dto.request.goong.DistanceTimeRequest;
import com.ralsei.dto.response.goong.CalculateRouteDistancesResponse;
import com.ralsei.dto.response.goong.DistanceTimeResponse;
import com.ralsei.dto.response.goong.GeocodeResponse;
import com.ralsei.model.RouteStop;
import java.util.List;

public interface GoongService {
    Object autocomplete(String input);
    DistanceTimeResponse getDistanceAndTime(DistanceTimeRequest request);
    CalculateRouteDistancesResponse calculateRouteDistances(CalculateRouteDistancesRequest request);
    void calculateAndSetRouteStopsDistances(List<RouteStop> sortedStops);
    GeocodeResponse geocode(String address);
}
