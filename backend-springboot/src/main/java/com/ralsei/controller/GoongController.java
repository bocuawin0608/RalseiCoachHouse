package com.ralsei.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ralsei.dto.request.goong.DistanceTimeRequest;
import com.ralsei.dto.request.goong.CalculateRouteDistancesRequest;
import com.ralsei.dto.response.goong.DistanceTimeResponse;
import com.ralsei.dto.response.goong.CalculateRouteDistancesResponse;
import com.ralsei.model.RouteStop;
import com.ralsei.repository.RouteStopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/v2/goong")
@RequiredArgsConstructor
public class GoongController {

    @Value("${goong.api.key}")
    private String goongApiKey;

    private final RestTemplate restTemplate;
    private final RouteStopRepository routeStopRepository;

    @GetMapping("/place/autocomplete")
    public ResponseEntity<Object> autocomplete(@RequestParam String input) {
        String url = "https://rsapi.goong.io/v2/place/autocomplete?api_key=" + goongApiKey + "&input=" + input;
        Object response = restTemplate.getForObject(url, Object.class);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/place/distance-time")
    public ResponseEntity<DistanceTimeResponse> getDistanceAndTime(@Valid @ModelAttribute DistanceTimeRequest request) {
        String origin = request.getOriginLat() + ", " + request.getOriginLng();
        String destination = request.getDestinationLat() + ", " + request.getDestinationLng();
        String url = "https://rsapi.goong.io/v2/direction?api_key=" + goongApiKey
                + "&origin=" + origin
                + "&destination=" + destination
                + "&vehicle=" + request.getVehicle();

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        JsonNode leg = response.path("routes").path(0).path("legs").path(0);

        // distance.value is in meters → convert to km
        double distanceKm = leg.path("distance").path("value").asDouble() / 1000.0;
        // duration.value is in seconds → convert to minutes
        double durationMinutes = leg.path("duration").path("value").asDouble() / 60.0;

        // Round to 2 decimal places
        distanceKm = Math.round(distanceKm * 100.0) / 100.0;
        durationMinutes = Math.round(durationMinutes * 100.0) / 100.0;

        return ResponseEntity.ok(new DistanceTimeResponse(distanceKm, durationMinutes));
    }

    /**
     * Calculate distance and time for all route stops using their CoachStop
     * lat/lng.
     * Steps:
     * 1. Fetch all route stops ordered by stopOrder (with CoachStop eagerly loaded)
     * 2. Use each CoachStop's latitude/longitude directly
     * 3. Call Goong Direction API for each consecutive pair
     * 4. Accumulate distance (km) and time (minutes) from the origin
     * 5. Update each RouteStop in DB
     */
    @PostMapping("/calculate-route-distances")
    @Transactional
    public ResponseEntity<CalculateRouteDistancesResponse> calculateRouteDistances(
            @Valid @RequestBody CalculateRouteDistancesRequest request) {
        int routeId = request.getRouteId();
        List<RouteStop> stops = routeStopRepository.findByRoute_RouteIdOrderByStopOrderAsc(routeId);

        if (stops.size() < 2) {
            return ResponseEntity.ok(CalculateRouteDistancesResponse.builder()
                    .message("Tuyến cần ít nhất 2 điểm dừng để tính khoảng cách.")
                    .updated(0)
                    .build());
        }

        // Build a single Direction API call
        String origin = stops.get(0).getCoachStop().getLatitude() + "," + stops.get(0).getCoachStop().getLongitude();

        StringBuilder destinationBuilder = new StringBuilder();
        for (int i = 1; i < stops.size(); i++) {
            if (i > 1) {
                destinationBuilder.append(";");
            }
            destinationBuilder.append(stops.get(i).getCoachStop().getLatitude())
                    .append(",")
                    .append(stops.get(i).getCoachStop().getLongitude());
        }
        String destination = destinationBuilder.toString();

        String url = "https://rsapi.goong.io/v2/direction?api_key=" + goongApiKey
                + "&origin=" + origin
                + "&destination=" + destination
                + "&vehicle=car";

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        JsonNode legs = response.path("routes").path(0).path("legs");

        // First stop always has 0 distance and 0 time
        stops.get(0).setKilometersFromStart(BigDecimal.ZERO);
        stops.get(0).setMinutesFromStart(0);

        double cumulativeKm = 0;
        double cumulativeMinutes = 0;
        int updated = 0;

        // Each leg corresponds to the segment from stop[i] → stop[i+1]
        for (int i = 0; i < legs.size(); i++) {
            JsonNode leg = legs.path(i);
            double distanceKm = leg.path("distance").path("value").asDouble() / 1000.0;
            double durationMinutes = leg.path("duration").path("value").asDouble() / 60.0;

            cumulativeKm += Math.round(distanceKm * 100.0) / 100.0;
            cumulativeMinutes += Math.round(durationMinutes * 100.0) / 100.0;

            BigDecimal km = BigDecimal.valueOf(cumulativeKm).setScale(2, RoundingMode.HALF_UP);
            int minutes = (int) Math.round(cumulativeMinutes);

            stops.get(i + 1).setKilometersFromStart(km);
            stops.get(i + 1).setMinutesFromStart(minutes);
            updated++;
        }

        routeStopRepository.saveAll(stops);

        return ResponseEntity.ok(CalculateRouteDistancesResponse.builder()
                .message("Đã tính toán khoảng cách và thời gian cho " + updated + " điểm dừng.")
                .updated(updated)
                .build());
    }
}
