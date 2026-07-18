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
/**
 * Provides the seat hold service impl component for the application.
 */
public class SeatHoldServiceImpl implements SeatHoldService {

    private final StringRedisTemplate redisTemplate;

    private String buildLockKey(Integer tripSeatId) {
        return "lock:trip_seat:" + tripSeatId;
    }

    private String buildSessionKey(String holdToken) {
        return "session:seats:" + holdToken;
    }

    @Override
    /**
     * Returns whether the seat locked is active.
     *
     * @param tripSeatId the value supplied for this operation
     *
     * @return {@code true} if the seat locked is active; otherwise {@code false}
     */
    public boolean isSeatLocked(Integer tripSeatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildLockKey(tripSeatId)));
    }

    @Override
    /**
     * Executes the lock seats operation.
     *
     * @param tripSeatIds the value supplied for this operation
     * @param holdToken the value supplied for this operation
     * @param TTLSeconds the value supplied for this operation
     *
     * @return the operation result
     */
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
    /**
     * Executes the release seats operation.
     *
     * @param tripSeatIds the value supplied for this operation
     * @param holdToken the value supplied for this operation
     *
     * @return the operation result
     */
    public boolean releaseSeats(List<Integer> tripSeatIds, String holdToken) {
        if (holdToken == null || holdToken.isBlank() || tripSeatIds == null || tripSeatIds.isEmpty()) {
            return true;
        }

        String sessionKey = buildSessionKey(holdToken);
        for (Integer tripSeatId : tripSeatIds) {
            if (tripSeatId == null) {
                continue;
            }
            String key = buildLockKey(tripSeatId);
            if (holdToken.equals(redisTemplate.opsForValue().get(key))) {
                redisTemplate.delete(key);
            }
            // Keep other held seats in the same session (multi-seat staff change).
            redisTemplate.opsForSet().remove(sessionKey, String.valueOf(tripSeatId));
        }

        Long remaining = redisTemplate.opsForSet().size(sessionKey);
        if (remaining == null || remaining == 0L) {
            redisTemplate.delete(sessionKey);
        }

        return true;
    }

    @Override
    /**
     * Returns the trip seat ids by token.
     *
     * @param holdToken the value supplied for this operation
     *
     * @return the trip seat ids by token
     */
    public List<Integer> getTripSeatIdsByToken(String holdToken) {
        Set<String> members = redisTemplate.opsForSet().members(buildSessionKey(holdToken));
        if(members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream().map(id -> Integer.valueOf(id)).toList();
    }

    @Override
    /**
     * Executes the extend lock operation.
     *
     * @param tripSeatIds the value supplied for this operation
     * @param holdToken the value supplied for this operation
     * @param newTtlSeconds the value supplied for this operation
     *
     * @return the operation result
     */
    public boolean extendLock(List<Integer> tripSeatIds, String holdToken, long newTtlSeconds) {
        for (Integer tripSeatId : tripSeatIds) {
            String lockKey = buildLockKey(tripSeatId);
            if (holdToken.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.expire(lockKey, Duration.ofSeconds(newTtlSeconds));
            }
        }
        
        String sessionKey = buildSessionKey(holdToken);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
            redisTemplate.expire(sessionKey, Duration.ofSeconds(newTtlSeconds));
        }
        return true;
    }

    @Override
    /**
     * Executes the force release seats by ids operation.
     *
     * @param tripSeatIds the value supplied for this operation
     */
    public void forceReleaseSeatsByIds(List<Integer> tripSeatIds) {
        if (tripSeatIds == null || tripSeatIds.isEmpty()) {
            return;
        }
        for (Integer tripSeatId : tripSeatIds) {
            redisTemplate.delete(buildLockKey(tripSeatId));
        }
    }
}
