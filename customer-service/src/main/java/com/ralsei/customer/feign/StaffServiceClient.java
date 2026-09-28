package com.ralsei.customer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "staff-service")
public interface StaffServiceClient {
    @GetMapping("/api/internal/trips/{tripId}")
    Object getTripById(@PathVariable("tripId") Integer tripId);

    @GetMapping("/api/internal/routes/{routeId}")
    Object getRouteById(@PathVariable("routeId") Integer routeId);
}
