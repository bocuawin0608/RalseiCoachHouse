package com.ralsei.dto.response.passengerbooking;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the response payload for seat lock operations.
 */
public record SeatLockResponse(
    List<Integer> tripSeatIds,
    String holdToken,
    LocalDateTime expiresAt
) {}
