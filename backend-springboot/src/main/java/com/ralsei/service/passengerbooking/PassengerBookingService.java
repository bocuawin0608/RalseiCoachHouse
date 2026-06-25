package com.ralsei.service.passengerbooking;

import java.util.List;

import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;

public interface PassengerBookingService {
    List<TripSeatResponse> getSeatMap(Integer tripId);
    SeatLockResponse lockSeats(Integer tripId, SeatLockRequest request, String holdToken);
    boolean releaseSeats(List<Integer> tripSeatIds, String holdToken);
}
