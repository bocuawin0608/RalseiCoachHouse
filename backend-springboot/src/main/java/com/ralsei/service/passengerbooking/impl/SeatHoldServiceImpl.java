package com.ralsei.service.passengerbooking.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ralsei.service.passengerbooking.SeatHoldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final StringRedisTemplate redisTemplate;
    private static final String LOCK_SEAT_PREFIX = "lock:trip_seat:";

    @Override
    public boolean isLocked(Integer tripSeatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOCK_SEAT_PREFIX + tripSeatId));
    }
}
