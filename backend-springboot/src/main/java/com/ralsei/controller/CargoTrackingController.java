package com.ralsei.controller;

import com.ralsei.dto.response.CargoTrackingResponse;
import com.ralsei.service.CargoTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cargo-tracking")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CargoTrackingController {

    private final CargoTrackingService cargoTrackingService;

    @GetMapping("/{ticketCode}")
    public ResponseEntity<CargoTrackingResponse> trackByCode(@PathVariable String ticketCode) {
        CargoTrackingResponse response = cargoTrackingService.trackByCode(ticketCode);
        return ResponseEntity.ok(response);
    }
}
