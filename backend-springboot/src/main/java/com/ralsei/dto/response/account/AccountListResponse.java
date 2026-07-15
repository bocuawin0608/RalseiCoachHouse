package com.ralsei.dto.response.account;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AccountListResponse
 */

/**
 * Represents the response payload for account list operations.
 */
public record AccountListResponse(
    Integer accountId,
    String username,
    String authProvider,
    @JsonProperty("active") boolean isActive,
    LocalDateTime lastLogin,
    List<String> roles,
    Integer staffId,
    String staffName,
    String staffPosition,
    String phone,
    String email,
    LocalDateTime createdAt,
    String customerName
) {}
