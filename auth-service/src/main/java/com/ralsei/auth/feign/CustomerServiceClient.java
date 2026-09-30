package com.ralsei.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service", url = "${customer.service.url:http://localhost:8082}")
public interface CustomerServiceClient {
    @PostMapping("/api/internal/customers")
    void createCustomer(@RequestBody Object request);
}
