package com.ralsei.service.passengerticket;

import java.util.List;

import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.request.staffpassengerticket.StaffPassengerChangePassengerRequest;
import com.ralsei.dto.request.staffpassengerticket.StaffPassengerChangeSeatRequest;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;

public interface StaffPassengerTicketChangeService {

    StaffPassengerTicketDetailResponse changePassengerInfo(
        Integer accountId,
        String ticketCode,
        Integer ticketDetailId,
        StaffPassengerChangePassengerRequest request
    );

    List<TripSeatResponse> getSeatMap(Integer tripId);

    SeatLockResponse lockSeats(Integer tripId, SeatLockRequest request, String holdToken);

    void releaseSeats(List<Integer> tripSeatIds, String holdToken);

    StaffPassengerTicketDetailResponse changeSeat(
        Integer accountId,
        String ticketCode,
        Integer ticketDetailId,
        StaffPassengerChangeSeatRequest request,
        String holdToken
    );
}
