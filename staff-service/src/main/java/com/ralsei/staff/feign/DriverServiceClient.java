package com.ralsei.staff.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "driver-service")
public interface DriverServiceClient {
    @GetMapping("/api/internal/coaches/{coachId}")
    Object getCoachById(@PathVariable("coachId") Integer coachId);

    @GetMapping("/api/internal/coaches/available")
    Object getAvailableCoaches(@RequestParam("routeId") Integer routeId, @RequestParam("departureTime") String departureTime);
}
