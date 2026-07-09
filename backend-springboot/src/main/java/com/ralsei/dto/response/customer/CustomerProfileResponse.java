package com.ralsei.dto.response.customer;

import java.time.LocalDate;
import java.util.List;

/**
 * Customer account profile displayed on the customer website.
 * Account and provider fields are read-only so the UI can explain which
 * authentication method owns sensitive credential changes.
 */
public record CustomerProfileResponse(
    Integer customerId,
    Integer accountId,
    String username,
    String customerName,
    String phone,
    String email,
    LocalDate dob,
    String authProvider,
    List<String> roles
) {}
