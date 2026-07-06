package com.ralsei.dto.request.customer;

/**
 * CustomerFilterRequest
 */

public record CustomerFilterRequest(
    String search,
    Boolean isActive
) {}
