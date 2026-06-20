package com.ralsei.service.passengerbooking.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.passengerbooking.PassengerBookingService;
import com.ralsei.service.passengerbooking.SeatHoldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PassengerBookingServiceImpl implements PassengerBookingService {

    private final TripRepository tripRepo;
    private final TripSeatRepository tripSeatRepo;

    private final SeatHoldService seatHoldService;
    
    @Transactional(readOnly = true)
    @Override
    public List<TripSeatResponse> getSeatMap(Integer tripId) {
        if(!tripRepo.existsById(tripId)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId);
        }
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé
        
        List<TripSeatResponse> tripSeats = tripSeatRepo.getSeatMap(tripId);
        
        return tripSeats.stream().map(seat -> {
            return seatHoldService.isLocked(seat.tripSeatId()) 
                ? seat.toBuilder().status(TripSeatStatus.LOCKED).build() : seat;
        }).toList();
    }
}
