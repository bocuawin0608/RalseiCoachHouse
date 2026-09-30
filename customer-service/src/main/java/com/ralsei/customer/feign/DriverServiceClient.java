package com.ralsei.customer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "driver-service", url = "${driver.service.url:http://localhost:8084}")
public interface DriverServiceClient {
    @GetMapping("/api/internal/coaches/{coachId}")
    Object getCoachById(@PathVariable("coachId") Integer coachId);

    @GetMapping("/api/internal/coaches/{coachId}/seats")
    Object getSeatsByCoachId(@PathVariable("coachId") Integer coachId);
}
