package com.ralsei.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "staff-service")
public interface StaffServiceClient {
    @GetMapping("/api/internal/staff/by-account/{accountId}")
    Object getStaffByAccountId(@PathVariable("accountId") Integer accountId);
}
