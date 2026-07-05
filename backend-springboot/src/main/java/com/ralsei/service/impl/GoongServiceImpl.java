package com.ralsei.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ralsei.dto.request.goong.CalculateRouteDistancesRequest;
import com.ralsei.dto.request.goong.DistanceTimeRequest;
import com.ralsei.dto.response.goong.CalculateRouteDistancesResponse;
import com.ralsei.dto.response.goong.DistanceTimeResponse;
import com.ralsei.model.Route;
import com.ralsei.model.RouteStop;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.service.GoongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoongServiceImpl implements GoongService {

    @Value("${goong.api.key}")
    private String goongApiKey;

    private final RestTemplate restTemplate;
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;

    @Override
    public Object autocomplete(String input) {
        String url = "https://rsapi.goong.io/v2/place/autocomplete?api_key=" + goongApiKey + "&input=" + input;
        return restTemplate.getForObject(url, Object.class);
    }

    @Override
    public DistanceTimeResponse getDistanceAndTime(DistanceTimeRequest request) {
        String origin = request.getOriginLat() + ", " + request.getOriginLng();
        String destination = request.getDestinationLat() + ", " + request.getDestinationLng();
        String url = "https://rsapi.goong.io/v2/direction?api_key=" + goongApiKey
                + "&origin=" + origin
                + "&destination=" + destination
                + "&vehicle=" + request.getVehicle();

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        if (response == null || !response.has("routes") || response.path("routes").isEmpty()) {
            throw new RuntimeException("Lỗi khi gọi Goong API");
        }
        JsonNode leg = response.path("routes").path(0).path("legs").path(0);

        double distanceKm = leg.path("distance").path("value").asDouble() / 1000.0;
        double durationMinutes = leg.path("duration").path("value").asDouble() / 60.0;

        distanceKm = Math.round(distanceKm * 100.0) / 100.0;
        durationMinutes = Math.round(durationMinutes * 100.0) / 100.0;

        return new DistanceTimeResponse(distanceKm, durationMinutes);
    }

    @Override
    @Transactional
    public CalculateRouteDistancesResponse calculateRouteDistances(CalculateRouteDistancesRequest request) {
        int routeId = request.getRouteId();
        List<RouteStop> stops = routeStopRepository.findByRoute_RouteIdOrderByStopOrderAsc(routeId);

        if (stops.size() < 2) {
            return CalculateRouteDistancesResponse.builder()
                    .message("Tuyến cần ít nhất 2 điểm dừng để tính khoảng cách.")
                    .updated(0)
                    .build();
        }

        calculateAndSetRouteStopsDistances(stops);
        routeStopRepository.saveAll(stops);

        // update route total kilometers and total minutes correponding to the last
        // stops
        Route route = stops.get(0).getRoute();
        route.setTotalKilometers(stops.get(stops.size() - 1).getKilometersFromStart());
        route.setTotalMinutes(stops.get(stops.size() - 1).getMinutesFromStart());
        routeRepository.save(route);

        return CalculateRouteDistancesResponse.builder()
                .message("Đã tính toán khoảng cách và thời gian cho " + (stops.size() - 1) + " điểm dừng.")
                .updated(stops.size() - 1)
                .build();
    }

    @Override
    public void calculateAndSetRouteStopsDistances(List<RouteStop> sortedStops) {
        if (sortedStops == null || sortedStops.size() < 2) {
            return;
        }

        String origin = sortedStops.get(0).getCoachStop().getLatitude() + ","
                + sortedStops.get(0).getCoachStop().getLongitude();

        StringBuilder destinationBuilder = new StringBuilder();
        for (int i = 1; i < sortedStops.size(); i++) {
            if (i > 1) {
                destinationBuilder.append(";");
            }
            destinationBuilder.append(sortedStops.get(i).getCoachStop().getLatitude())
                    .append(",")
                    .append(sortedStops.get(i).getCoachStop().getLongitude());
        }
        String destination = destinationBuilder.toString();

        String url = "https://rsapi.goong.io/v2/direction?api_key=" + goongApiKey
                + "&origin=" + origin
                + "&destination=" + destination
                + "&vehicle=car";

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        if (response == null || !response.has("routes") || response.path("routes").isEmpty()) {
            throw new RuntimeException("Lỗi khi tính toán khoảng cách từ Goong API");
        }
        JsonNode legs = response.path("routes").path(0).path("legs");

        sortedStops.get(0).setKilometersFromStart(BigDecimal.ZERO);
        sortedStops.get(0).setMinutesFromStart(0);

        double cumulativeKm = 0;
        double cumulativeMinutes = 0;

        for (int i = 0; i < legs.size(); i++) {
            JsonNode leg = legs.path(i);
            double distanceKm = leg.path("distance").path("value").asDouble() / 1000.0;
            double durationMinutes = leg.path("duration").path("value").asDouble() / 60.0;

            cumulativeKm += Math.round(distanceKm * 100.0) / 100.0;
            cumulativeMinutes += Math.round(durationMinutes * 100.0) / 100.0;

            BigDecimal km = BigDecimal.valueOf(cumulativeKm).setScale(2, RoundingMode.HALF_UP);
            int minutes = (int) Math.round(cumulativeMinutes);

            sortedStops.get(i + 1).setKilometersFromStart(km);
            sortedStops.get(i + 1).setMinutesFromStart(minutes);
        }
    }
}
