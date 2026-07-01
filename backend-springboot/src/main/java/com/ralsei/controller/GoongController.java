package com.ralsei.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ralsei.dto.request.goong.DistanceTimeRequest;
import com.ralsei.dto.response.goong.DistanceTimeResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v2/goong")
public class GoongController {

    @Value("${goong.api.key}")
    private String goongApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

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
}
