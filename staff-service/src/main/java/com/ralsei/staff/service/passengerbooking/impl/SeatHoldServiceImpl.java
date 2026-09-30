package com.ralsei.staff.service.passengerbooking.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ralsei.staff.service.passengerbooking.SeatHoldService;

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

    private String buildVacatedKey(String holdToken) {
        return "session:vacated:" + holdToken;
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
            redisTemplate.opsForSet().remove(sessionKey, String.valueOf(tripSeatId));
        }

        Long remaining = redisTemplate.opsForSet().size(sessionKey);
        if (remaining == null || remaining == 0L) {
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(buildVacatedKey(holdToken));
        }

        return true;
    }

    @Override
    public List<Integer> getTripSeatIdsByToken(String holdToken) {
        return membersAsIntegers(buildSessionKey(holdToken));
    }

    @Override
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
        String vacatedKey = buildVacatedKey(holdToken);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(vacatedKey))) {
            redisTemplate.expire(vacatedKey, Duration.ofSeconds(newTtlSeconds));
        }
        return true;
    }

    @Override
    public void forceReleaseSeatsByIds(List<Integer> tripSeatIds) {
        if (tripSeatIds == null || tripSeatIds.isEmpty()) {
            return;
        }
        for (Integer tripSeatId : tripSeatIds) {
            redisTemplate.delete(buildLockKey(tripSeatId));
        }
    }

    @Override
    public void markVacated(String holdToken, List<Integer> tripSeatIds, long ttlSeconds) {
        if (holdToken == null || holdToken.isBlank() || tripSeatIds == null || tripSeatIds.isEmpty()) {
            return;
        }
        List<String> values = tripSeatIds.stream()
            .filter(id -> id != null && id > 0)
            .map(String::valueOf)
            .toList();
        if (values.isEmpty()) {
            return;
        }
        String vacatedKey = buildVacatedKey(holdToken);
        redisTemplate.opsForSet().add(vacatedKey, values.toArray(String[]::new));
        redisTemplate.expire(vacatedKey, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void clearVacated(String holdToken, List<Integer> tripSeatIds) {
        if (holdToken == null || holdToken.isBlank() || tripSeatIds == null || tripSeatIds.isEmpty()) {
            return;
        }
        String vacatedKey = buildVacatedKey(holdToken);
        for (Integer tripSeatId : tripSeatIds) {
            if (tripSeatId != null) {
                redisTemplate.opsForSet().remove(vacatedKey, String.valueOf(tripSeatId));
            }
        }
        Long remaining = redisTemplate.opsForSet().size(vacatedKey);
        if (remaining == null || remaining == 0L) {
            redisTemplate.delete(vacatedKey);
        }
    }

    @Override
    public List<Integer> getVacatedSeatIdsByToken(String holdToken) {
        return membersAsIntegers(buildVacatedKey(holdToken));
    }

    private List<Integer> membersAsIntegers(String key) {
        Set<String> members = redisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream().map(Integer::valueOf).toList();
    }
}
