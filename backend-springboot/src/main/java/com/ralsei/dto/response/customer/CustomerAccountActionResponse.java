package com.ralsei.dto.response.customer;

/**
 * Small command response for customer self-service account actions.
 */
/**
 * Represents the response payload for customer account action operations.
 */
public record CustomerAccountActionResponse(
    boolean success,
    String message
) {}
