package com.ralsei.dto.response.role;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RoleListResponse
 */

/**
 * Represents the response payload for role list operations.
 */
public record RoleListResponse(
    Integer roleId,
    String roleName,
    @JsonProperty("active") boolean isActive,
    long assignedCount,
    LocalDateTime createdAt
) {}
