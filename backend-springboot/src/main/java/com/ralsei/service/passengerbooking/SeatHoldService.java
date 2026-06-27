package com.ralsei.service.passengerbooking;

import java.util.List;

public interface SeatHoldService {
    boolean isSeatLocked(Integer tripSeatId);
    boolean lockSeats(List<Integer> tripSeatIds, String holdToken, long ttlSeconds);
    boolean releaseSeats(List<Integer> tripSeatIds, String holdToken);
    List<Integer> getTripSeatIdsByToken(String holdToken);
    boolean extendLock(List<Integer> tripSeatIds, String holdToken, long newTtlSeconds);
}
