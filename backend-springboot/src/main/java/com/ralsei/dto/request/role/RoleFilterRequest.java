package com.ralsei.dto.request.role;

/**
 * RoleFilterRequest
 */

public record RoleFilterRequest(
    String search,
    Boolean isActive
) {}
