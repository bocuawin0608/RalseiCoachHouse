package com.ralsei.dto.response.role;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoleListResponse(
    Integer roleId,
    String roleName,
    @JsonProperty("active") boolean isActive,
    long assignedCount,
    LocalDateTime createdAt
) {}
