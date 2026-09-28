package com.ralsei.staff.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/trips")
@RequiredArgsConstructor
public class InternalTripController {
    // GET /{tripId} -> returns TripInfoDTO
    // GET /{tripId}/stops -> returns List<TripStopDTO>
    // POST /batch -> body: List<Integer> tripIds -> returns List<TripInfoDTO>
}
