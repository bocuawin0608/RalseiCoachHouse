package com.ralsei.staff.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    @GetMapping("/api/internal/passenger-tickets/count-by-trip/{tripId}")
    Integer countTicketsByTripId(@PathVariable("tripId") Integer tripId);

    @GetMapping("/api/internal/trip-seats/count-by-trip/{tripId}")
    Object getSeatAvailabilityByTripId(@PathVariable("tripId") Integer tripId);
}
