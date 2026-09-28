package com.ralsei.customer.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {
    // GET /{customerId} -> returns CustomerInfoDTO
    // POST /batch -> body: List<Integer> customerIds -> returns List<CustomerInfoDTO>
}
