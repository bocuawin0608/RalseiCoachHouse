package com.ralsei.dto.response.account;

import java.time.LocalDateTime;
import java.util.List;

public record AccountListResponse(
    Integer accountId,
    String username,
    String authProvider,
    boolean isActive,
    LocalDateTime lastLogin,
    List<String> roles,
    Integer staffId,
    String staffName,
    String staffPosition,
    LocalDateTime createdAt
) {}
