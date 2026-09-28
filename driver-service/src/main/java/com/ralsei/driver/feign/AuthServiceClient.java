package com.ralsei.driver.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "auth-service", url = "${feign.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {
    // Defines endpoints to fetch auth data
}
