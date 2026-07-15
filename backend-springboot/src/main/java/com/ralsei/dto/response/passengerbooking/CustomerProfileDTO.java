package com.ralsei.dto.response.passengerbooking;

/**
 * Represents the data transfer object for customer profile.
 */
public record CustomerProfileDTO(
    String fullname,
    String phone,
    String email
) {}
