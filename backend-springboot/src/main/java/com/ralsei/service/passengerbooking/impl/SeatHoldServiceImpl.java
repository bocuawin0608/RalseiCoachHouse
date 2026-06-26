package com.ralsei.service.passengerbooking.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ralsei.service.passengerbooking.SeatHoldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {

    private final StringRedisTemplate redisTemplate;

    private String buildLockKey(Integer tripSeatId) {
        return "lock:trip_seat:" + tripSeatId;
    }

    private String buildSessionKey(String holdToken) {
        return "session:seats:" + holdToken;
    }

    @Override
    public boolean isSeatLocked(Integer tripSeatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildLockKey(tripSeatId)));
    }

    @Override
    public boolean lockSeats(List<Integer> tripSeatIds, String holdToken, long TTLSeconds) {
        List<Integer> lockedSoFar = new ArrayList<>();
        for (Integer tripSeatId : tripSeatIds) {
            if(redisTemplate.opsForValue().setIfAbsent(buildLockKey(tripSeatId), holdToken, Duration.ofSeconds(TTLSeconds))){
                lockedSoFar.add(tripSeatId);
            } else {
                releaseSeats(lockedSoFar, holdToken);
                return false;
            }
        }

        String sessionKey = buildSessionKey(holdToken);
        String[] stringTripSeatIds = tripSeatIds.stream().map(id -> String.valueOf(id)).toArray(size -> new String[size]);
        redisTemplate.opsForSet().add(sessionKey, stringTripSeatIds);
        redisTemplate.expire(sessionKey, Duration.ofSeconds(TTLSeconds));

        return true;
    }

    @Override
    public boolean releaseSeats(List<Integer> tripSeatIds, String holdToken) {
        for (Integer tripSeatId : tripSeatIds) {
            String key = buildLockKey(tripSeatId);
            if(holdToken.equals(redisTemplate.opsForValue().get(key))){
                redisTemplate.delete(key);
            }
        }

        redisTemplate.delete(buildSessionKey(holdToken));

        return true;
    }

    @Override
    public List<Integer> getTripSeatIdsByToken(String holdToken) {
        Set<String> members = redisTemplate.opsForSet().members(buildSessionKey(holdToken));
        if(members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream().map(id -> Integer.valueOf(id)).toList();
    }
}
