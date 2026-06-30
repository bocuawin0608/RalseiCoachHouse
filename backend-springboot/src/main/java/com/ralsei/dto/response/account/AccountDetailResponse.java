package com.ralsei.dto.response.account;

import java.time.LocalDateTime;
import java.util.List;

public record AccountDetailResponse(
    Integer accountId,
    String username,
    String authProvider,
    boolean isActive,
    LocalDateTime lastLogin,
    List<RoleResponse> roles,
    StaffInfoResponse staff,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy
) {}
