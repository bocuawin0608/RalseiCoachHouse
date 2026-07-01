package com.ralsei.dto.request.role;

public record RoleFilterRequest(
    String search,
    Boolean isActive
) {}
