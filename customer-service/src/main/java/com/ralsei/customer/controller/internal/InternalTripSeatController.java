package com.ralsei.customer.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/trip-seats")
@RequiredArgsConstructor
public class InternalTripSeatController {
    // GET /count-by-trip/{tripId} -> SeatAvailabilityDTO
}
