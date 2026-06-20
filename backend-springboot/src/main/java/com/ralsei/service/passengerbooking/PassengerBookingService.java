package com.ralsei.service.passengerbooking;

import java.util.List;

import com.ralsei.dto.response.passengerbooking.TripSeatResponse;

public interface PassengerBookingService {
    List<TripSeatResponse> getSeatMap(Integer tripId);
}
