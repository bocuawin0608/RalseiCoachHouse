package com.ralsei.dto.response.passengerbooking;

import java.time.LocalDateTime;
import java.util.List;

public record SeatLockResponse(
    List<Integer> tripSeatIds,
    String holdToken,
    LocalDateTime expiresAt
) {}
