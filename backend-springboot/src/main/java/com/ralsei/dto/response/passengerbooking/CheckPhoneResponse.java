package com.ralsei.dto.response.passengerbooking;

/**
 * Represents the response payload for check phone operations.
 */
public record CheckPhoneResponse(
    boolean isKnown,
    SuggestedPassengerProfile suggestedProfile
) {}
