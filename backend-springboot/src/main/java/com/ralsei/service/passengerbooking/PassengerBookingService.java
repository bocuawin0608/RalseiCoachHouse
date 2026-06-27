package com.ralsei.service.passengerbooking;

import java.util.List;

import com.ralsei.dto.request.passengerbooking.BookingConfirmRequest;
import com.ralsei.dto.request.passengerbooking.PriceCalculationRequest;
import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.response.passengerbooking.BookingConfirmResponse;
import com.ralsei.dto.response.passengerbooking.PriceCalculationResponse;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.Step2InitResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;

public interface PassengerBookingService {
    List<TripSeatResponse> getSeatMap(Integer tripId);
    SeatLockResponse lockSeats(Integer tripId, SeatLockRequest request, String holdToken);
    boolean releaseSeats(List<Integer> tripSeatIds, String holdToken);
    boolean releaseSeatsByBecon(String holdToken);

    Step2InitResponse getStep2InitData(Integer tripId, String holdToken, String accessToken);
    PriceCalculationResponse calculatePrice(Integer tripId, PriceCalculationRequest request, String accessToken);
    BookingConfirmResponse confirmBooking(Integer tripId, BookingConfirmRequest request, String holdToken, String accessToken);
}
