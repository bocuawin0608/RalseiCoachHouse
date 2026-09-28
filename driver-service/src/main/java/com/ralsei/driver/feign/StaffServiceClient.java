package com.ralsei.driver.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "staff-service", url = "${feign.staff-service.url:http://localhost:8083}")
public interface StaffServiceClient {
    // Defines endpoints to fetch route data, etc.
}
