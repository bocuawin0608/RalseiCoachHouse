package com.ralsei.controller;

import com.ralsei.dto.request.goong.DistanceTimeRequest;
import com.ralsei.dto.request.goong.CalculateRouteDistancesRequest;
import com.ralsei.dto.response.goong.DistanceTimeResponse;
import com.ralsei.dto.response.goong.CalculateRouteDistancesResponse;
import com.ralsei.service.GoongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/goong")
@RequiredArgsConstructor
public class GoongController {

    private final GoongService goongService;

    @GetMapping("/place/autocomplete")
    public ResponseEntity<Object> autocomplete(@RequestParam String input) {
        return ResponseEntity.ok(goongService.autocomplete(input));
    }

    @GetMapping("/place/distance-time")
    public ResponseEntity<DistanceTimeResponse> getDistanceAndTime(@Valid @ModelAttribute DistanceTimeRequest request) {
        return ResponseEntity.ok(goongService.getDistanceAndTime(request));
    }

    @PostMapping("/calculate-route-distances")
    public ResponseEntity<CalculateRouteDistancesResponse> calculateRouteDistances(
            @Valid @RequestBody CalculateRouteDistancesRequest request) {
        return ResponseEntity.ok(goongService.calculateRouteDistances(request));
    }
}
