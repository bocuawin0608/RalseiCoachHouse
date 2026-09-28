package com.ralsei.customer.dto.request.customer;

/**
 * Represents the request payload for customer filter operations.
 */
public record CustomerFilterRequest(
    String search,
    Boolean isActive,
    String accountType,
    String activity
) {}
