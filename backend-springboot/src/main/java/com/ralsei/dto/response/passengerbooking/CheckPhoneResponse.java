package com.ralsei.dto.response.passengerbooking;

public record CheckPhoneResponse(
    boolean isKnown,
    SuggestedPassengerProfile suggestedProfile
) {}
