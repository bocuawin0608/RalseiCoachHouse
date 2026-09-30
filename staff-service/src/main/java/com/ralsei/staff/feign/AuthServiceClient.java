package com.ralsei.staff.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${auth.service.url:http://localhost:8081}")
public interface AuthServiceClient {
    @GetMapping("/api/internal/accounts/{accountId}")
    Object getAccountById(@PathVariable("accountId") Integer accountId);
}
