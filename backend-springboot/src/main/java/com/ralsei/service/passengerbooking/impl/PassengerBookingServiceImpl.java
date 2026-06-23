package com.ralsei.service.passengerbooking.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.passengerbooking.PassengerBookingService;
import com.ralsei.service.passengerbooking.SeatHoldService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerBookingServiceImpl implements PassengerBookingService {

    private final TripRepository tripRepo;
    private final TripSeatRepository tripSeatRepo;

    private final SeatHoldService seatHoldService;

    private static final long BROWSING_HOLD_TTL_SECONDS = 600;
    
    @Transactional(readOnly = true)
    @Override
    public List<TripSeatResponse> getSeatMap(Integer tripId) {
        if(!tripRepo.existsById(tripId)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId);
        }
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé
        
        List<TripSeatResponse> tripSeats = tripSeatRepo.getSeatMap(tripId);
        
        return tripSeats.stream().map(seat -> {
            return seatHoldService.isSeatLocked(seat.tripSeatId()) 
                ? seat.toBuilder().status(TripSeatStatus.LOCKED).build() : seat;
        }).toList();
    }

    @Transactional
    @Override
    public SeatLockResponse lockSeats(Integer tripId, SeatLockRequest request, String holdToken) {
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé

        List<Integer> availableSeatIds = tripSeatRepo.findTripSeatIdsByTripIdAndStatus(tripId, TripSeatStatus.AVAILABLE);
        if(availableSeatIds.isEmpty() || !availableSeatIds.containsAll(request.tripSeatIds())) {
            throw new BusinessRuleException("Có ghế ngồi không hợp lệ hoặc đã được đặt! Vui lòng chọn lại!");
        }
        
        if(!seatHoldService.lockSeats(request.tripSeatIds(), holdToken, BROWSING_HOLD_TTL_SECONDS)) {
            throw new BusinessRuleException("Có ghế ngồi đã được đặt! Vui lòng chọn lại!");
        }

        return new SeatLockResponse(
            request.tripSeatIds(),
            holdToken,
            LocalDateTime.now().plusSeconds(BROWSING_HOLD_TTL_SECONDS)
        );
    }

    @Transactional
    @Override
    public boolean releaseSeats(List<Integer> tripSeatIds, String holdToken) {
        if (tripSeatIds == null || tripSeatIds.isEmpty()) {
            return false; 
        }

        try {
            seatHoldService.releaseSeats(tripSeatIds, holdToken);
            return true;
        } catch (Exception e) {
            log.error("Có lỗi xảy ra khi dọn dẹp giải phóng ghế: ", e);
            return false;
        }
    }
}
