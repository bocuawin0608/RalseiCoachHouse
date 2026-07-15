package com.ralsei.dto.response.role;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RoleDetailResponse
 */

/**
 * Represents the response payload for role detail operations.
 */
public record RoleDetailResponse(
    Integer roleId,
    String roleName,
    @JsonProperty("active") boolean isActive,
    long assignedCount,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy
) {}
