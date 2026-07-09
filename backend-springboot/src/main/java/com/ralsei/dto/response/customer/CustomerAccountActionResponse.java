package com.ralsei.dto.response.customer;

/**
 * Small command response for customer self-service account actions.
 */
public record CustomerAccountActionResponse(
    boolean success,
    String message
) {}
