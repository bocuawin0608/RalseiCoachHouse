package com.ralsei.controller;

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
    public ResponseEntity<Object> getDistanceAndTime(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destinationLat,
            @RequestParam double destinationLng,
            @RequestParam(defaultValue = "car") String vehicle) {
        String origin = originLat + ", " + originLng;
        String destination = destinationLat + ", " + destinationLng;
        String url = "https://rsapi.goong.io/v2/direction?api_key=" + goongApiKey
                + "&origin=" + origin
                + "&destination=" + destination
                + "&vehicle=" + vehicle;
        Object response = restTemplate.getForObject(url, Object.class);
        return ResponseEntity.ok(response);
    }
}
