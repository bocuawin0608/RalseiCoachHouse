package com.ralsei.dto.request.account;

/**
 * AccountFilterRequest
 */

/**
 * Represents the request payload for account filter operations.
 */
public record AccountFilterRequest(
    String search,
    String role,
    Boolean isActive,
    String staffPosition,
    String authProvider
) {}
