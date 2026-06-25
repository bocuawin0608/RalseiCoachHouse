package com.ralsei.service.passengerbooking.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ralsei.service.passengerbooking.SeatHoldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final StringRedisTemplate redisTemplate;

    private String buildKey(Integer tripSeatId) {
        return "lock:trip_seat:" + tripSeatId;
    }

    @Override
    public boolean isSeatLocked(Integer tripSeatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(tripSeatId)));
    }

    @Override
    public boolean lockSeats(List<Integer> tripSeatIds, String holdToken, long TTLSeconds) {
        List<Integer> lockedSoFar = new ArrayList<>();
        for (Integer tripSeatId : tripSeatIds) {
            if(redisTemplate.opsForValue().setIfAbsent(buildKey(tripSeatId), holdToken, Duration.ofSeconds(TTLSeconds))){
                lockedSoFar.add(tripSeatId);
            } else {
                releaseSeats(lockedSoFar, holdToken);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean releaseSeats(List<Integer> tripSeatIds, String holdToken) {
        for (Integer tripSeatId : tripSeatIds) {
            String key = buildKey(tripSeatId);
            if(holdToken.equals(redisTemplate.opsForValue().get(key))){
                redisTemplate.delete(key);
            }
        }
        return true;
    }


}
