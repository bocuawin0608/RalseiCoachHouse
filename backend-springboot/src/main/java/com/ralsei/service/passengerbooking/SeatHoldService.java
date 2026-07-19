package com.ralsei.service.passengerbooking;

import java.util.List;

/**
 * Provides the business service contract for seat hold.
 */
public interface SeatHoldService {
    boolean isSeatLocked(Integer tripSeatId);
    boolean lockSeats(List<Integer> tripSeatIds, String holdToken, long ttlSeconds);
    boolean releaseSeats(List<Integer> tripSeatIds, String holdToken);
    List<Integer> getTripSeatIdsByToken(String holdToken);
    boolean extendLock(List<Integer> tripSeatIds, String holdToken, long newTtlSeconds);

    void forceReleaseSeatsByIds(List<Integer> tripSeatIds);

    /** Marks trip seats as vacated within this hold session (same-ticket seat swaps). */
    void markVacated(String holdToken, List<Integer> tripSeatIds, long ttlSeconds);

    void clearVacated(String holdToken, List<Integer> tripSeatIds);

    List<Integer> getVacatedSeatIdsByToken(String holdToken);
}
