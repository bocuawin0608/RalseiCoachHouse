package com.ralsei.staff.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/routes")
@RequiredArgsConstructor
public class InternalRouteController {
    // GET /{routeId} -> returns RouteInfoDTO
    // GET /{routeId}/stops -> returns List<RouteStopDTO>
}
