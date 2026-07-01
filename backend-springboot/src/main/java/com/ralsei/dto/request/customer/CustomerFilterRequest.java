package com.ralsei.dto.request.customer;

public record CustomerFilterRequest(
    String search,
    Boolean isActive
) {}
