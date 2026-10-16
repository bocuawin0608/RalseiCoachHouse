package com.ralsei.customer.controller.internal;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.customer.model.Customer;
import com.ralsei.customer.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/internal/customers")
public class InternalCustomerController {

    private final CustomerRepository customerRepository;

    public InternalCustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer customerId) {
        return customerRepository.findById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<Customer> getCustomerByAccountId(@PathVariable Integer accountId) {
        return customerRepository.findByAccountId(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Customer>> getCustomersBatch(@RequestBody List<Integer> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(customerRepository.findAllById(customerIds));
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Map<String, Object> req) {
        Integer accountId = req.get("accountId") != null ? ((Number) req.get("accountId")).intValue() : null;
        if (accountId != null) {
            var existing = customerRepository.findByAccountId(accountId);
            if (existing.isPresent()) {
                return ResponseEntity.ok(existing.get());
            }
        }

        String name = (String) req.getOrDefault("customerName", "Khách hàng");
        String phone = (String) req.getOrDefault("phone", req.get("username"));
        String email = (String) req.get("email");

        Customer customer = Customer.builder()
                .accountId(accountId)
                .customerName(name != null && !name.isBlank() ? name : "Khách hàng")
                .phone(phone)
                .email(email)
                .isActive(true)
                .build();

        customer = customerRepository.save(customer);
        log.info("Created internal customer profile for accountId={}", accountId);
        return ResponseEntity.ok(customer);
    }
}
