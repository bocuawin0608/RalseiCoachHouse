package com.ralsei.dto.response.account;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AccountDetailResponse
 */

/**
 * Represents the response payload for account detail operations.
 */
public record AccountDetailResponse(
    Integer accountId,
    String username,
    String authProvider,
    @JsonProperty("active") boolean isActive,
    LocalDateTime lastLogin,
    List<RoleResponse> roles,
    StaffInfoResponse staff,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy
) {}
