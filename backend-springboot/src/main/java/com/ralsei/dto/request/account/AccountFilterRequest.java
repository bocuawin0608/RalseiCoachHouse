package com.ralsei.dto.request.account;

public record AccountFilterRequest(
    String search,
    String role,
    Boolean isActive,
    String staffPosition,
    String authProvider
) {}
