package com.ralsei.dto.request.role;

/**
 * RoleFilterRequest
 */

/**
 * Represents the request payload for role filter operations.
 */
public record RoleFilterRequest(
    String search,
    Boolean isActive
) {}
