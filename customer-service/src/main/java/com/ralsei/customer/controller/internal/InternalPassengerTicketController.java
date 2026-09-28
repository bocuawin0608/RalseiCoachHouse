package com.ralsei.customer.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/passenger-tickets")
@RequiredArgsConstructor
public class InternalPassengerTicketController {
    // GET /count-by-trip/{tripId} -> Integer
    // GET /by-trip/{tripId} -> List<PassengerTicketInfoDTO>
}
