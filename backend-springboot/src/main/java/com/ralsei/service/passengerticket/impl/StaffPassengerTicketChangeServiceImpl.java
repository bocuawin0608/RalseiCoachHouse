package com.ralsei.service.passengerticket.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.staffpassengerticket.StaffPassengerTicketRowProjection;
import com.ralsei.dto.request.passengerbooking.AccompaniedChildDTO;
import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.request.staffpassengerticket.StaffPassengerChangePassengerRequest;
import com.ralsei.dto.request.staffpassengerticket.StaffPassengerChangeSeatRequest;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.AccompaniedChild;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Trip;
import com.ralsei.model.TripSeat;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.AccompaniedChildRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.passengerbooking.SeatHoldService;
import com.ralsei.service.passengerticket.PassengerTicketStaffPolicy;
import com.ralsei.service.passengerticket.StaffPassengerTicketChangeService;
import com.ralsei.service.passengerticket.StaffPassengerTicketQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffPassengerTicketChangeServiceImpl implements StaffPassengerTicketChangeService {

    private static final long STAFF_SEAT_HOLD_TTL_SECONDS = 300;

    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final PassengerTicketRepository ticketRepository;
    private final AccompaniedChildRepository accompaniedChildRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final StaffRepository staffRepository;
    private final SeatHoldService seatHoldService;
    private final PassengerTicketStaffPolicy policy;
    private final StaffPassengerTicketQueryService queryService;

    @Override
    @Transactional
    public StaffPassengerTicketDetailResponse changePassengerInfo(
        Integer accountId,
        String ticketCode,
        Integer ticketDetailId,
        StaffPassengerChangePassengerRequest request
    ) {
        staffRepository.findByAccountId(accountId)
            .orElseThrow(() -> new BusinessRuleException("Không tìm thấy thông tin nhân viên!"));

        String normalizedTicketCode = ticketCode == null ? "" : ticketCode.trim();
        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByTicketCode(normalizedTicketCode);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy vé.");
        }

        StaffPassengerTicketRowProjection targetRow = rows.stream()
            .filter(row -> ticketDetailId.equals(row.getTicketDetailId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế trong vé này."));

        Trip trip = tripRepository.findById(targetRow.getTripId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe."));

        policy.assertPassengerInfoChangeAllowed(
            targetRow.getTicketStatus(),
            targetRow.getDetailStatus(),
            targetRow.getPaymentStatus(),
            targetRow.getDepartureTime(),
            trip.getStatus()
        );

        validateAccompaniedChildRequest(request);

        PassengerTicketDetail detail = ticketDetailRepository.findById(ticketDetailId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế trong vé này."));

        detail.setFullName(request.fullName().trim());
        detail.setPhone(request.phone().trim());
        detail.setEmail(request.email().trim());
        detail.setUpdatedBy(accountId);
        ticketDetailRepository.save(detail);

        syncAccompaniedChild(ticketDetailId, accountId, request);

        return queryService.getDetail(normalizedTicketCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripSeatResponse> getSeatMap(Integer tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId);
        }

        return tripSeatRepository.getSeatMap(tripId).stream()
            .map(seat -> seatHoldService.isSeatLocked(seat.tripSeatId())
                ? seat.toBuilder().status(TripSeatStatus.LOCKED).build()
                : seat)
            .toList();
    }

    @Override
    @Transactional
    public SeatLockResponse lockSeats(Integer tripId, SeatLockRequest request, String holdToken) {
        validateHoldSession(holdToken);

        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId);
        }

        if (request.tripSeatIds().size() != 1) {
            throw new BusinessRuleException("Chỉ được giữ một ghế khi đổi chỗ.");
        }

        Integer tripSeatId = request.tripSeatIds().get(0);
        List<Integer> availableSeatIds = tripSeatRepository.findTripSeatIdsByTripIdAndStatus(
            tripId, TripSeatStatus.AVAILABLE
        );

        if (!availableSeatIds.contains(tripSeatId)) {
            throw new BusinessRuleException("Ghế đã được đặt hoặc không khả dụng. Vui lòng chọn ghế khác.");
        }

        if (!seatHoldService.lockSeats(request.tripSeatIds(), holdToken, STAFF_SEAT_HOLD_TTL_SECONDS)) {
            throw new BusinessRuleException("Ghế vừa được giữ bởi người khác. Vui lòng chọn ghế khác.");
        }

        return new SeatLockResponse(
            request.tripSeatIds(),
            holdToken,
            LocalDateTime.now().plusSeconds(STAFF_SEAT_HOLD_TTL_SECONDS)
        );
    }

    @Override
    @Transactional
    public void releaseSeats(List<Integer> tripSeatIds, String holdToken) {
        if (holdToken == null || holdToken.isBlank() || tripSeatIds == null || tripSeatIds.isEmpty()) {
            return;
        }
        seatHoldService.releaseSeats(tripSeatIds, holdToken);
    }

    @Override
    @Transactional
    public StaffPassengerTicketDetailResponse changeSeat(
        Integer accountId,
        String ticketCode,
        Integer ticketDetailId,
        StaffPassengerChangeSeatRequest request,
        String holdToken
    ) {
        validateHoldSession(holdToken);

        staffRepository.findByAccountId(accountId)
            .orElseThrow(() -> new BusinessRuleException("Không tìm thấy thông tin nhân viên!"));

        String normalizedTicketCode = ticketCode == null ? "" : ticketCode.trim();
        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByTicketCode(normalizedTicketCode);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy vé.");
        }

        StaffPassengerTicketRowProjection targetRow = rows.stream()
            .filter(row -> ticketDetailId.equals(row.getTicketDetailId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế trong vé này."));

        Trip trip = tripRepository.findById(targetRow.getTripId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe."));

        int currentTripSeatId = targetRow.getTripSeatId() != null ? targetRow.getTripSeatId() : 0;
        int newTripSeatId = request.newTripSeatId();

        policy.assertChangeSeatAllowed(
            targetRow.getTicketStatus(),
            targetRow.getDetailStatus(),
            targetRow.getPaymentStatus(),
            targetRow.getDepartureTime(),
            trip.getStatus(),
            currentTripSeatId,
            newTripSeatId
        );

        List<Integer> lockedSeatIds = seatHoldService.getTripSeatIdsByToken(holdToken);
        if (!lockedSeatIds.contains(newTripSeatId)) {
            throw new BusinessRuleException("Phiên giữ ghế không hợp lệ hoặc đã hết hạn. Vui lòng chọn lại ghế.");
        }

        List<TripSeat> newSeats = tripSeatRepository.findByTripIdAndTripSeatIdInWithSeat(
            targetRow.getTripId(),
            List.of(newTripSeatId)
        );
        if (newSeats.size() != 1) {
            throw new BusinessRuleException("Ghế mới không thuộc chuyến xe này.");
        }

        TripSeat newSeat = newSeats.get(0);
        if (newSeat.getStatus() != TripSeatStatus.AVAILABLE) {
            throw new BusinessRuleException("Ghế mới không còn trống. Vui lòng chọn ghế khác.");
        }

        PassengerTicketDetail detail = ticketDetailRepository.findById(ticketDetailId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế trong vé này."));

        int oldTripSeatId = detail.getTripSeatId();
        if (oldTripSeatId > 0) {
            tripSeatRepository.updateStatusByTripSeatIds(List.of(oldTripSeatId), TripSeatStatus.AVAILABLE);
        }

        tripSeatRepository.updateStatusByTripSeatIds(List.of(newTripSeatId), TripSeatStatus.SOLD);

        detail.setTripSeatId(newTripSeatId);
        detail.setSeatCodeSnapshot(newSeat.getSeat().getSeatCode());
        detail.setUpdatedBy(accountId);
        ticketDetailRepository.save(detail);

        markTicketChangedIfNeeded(targetRow.getPassengerTicketId(), targetRow.getTicketStatus(), accountId);

        seatHoldService.releaseSeats(List.of(newTripSeatId), holdToken);

        return queryService.getDetail(normalizedTicketCode);
    }

    private void markTicketChangedIfNeeded(Integer passengerTicketId, String ticketStatus, Integer accountId) {
        if (PassengerTicketStatus.CONFIRMED.name().equals(ticketStatus)) {
            int updated = ticketRepository.updateStatusIfCurrent(
                passengerTicketId,
                PassengerTicketStatus.CONFIRMED,
                PassengerTicketStatus.CHANGED
            );
            if (updated != 1) {
                throw new BusinessRuleException("Trạng thái vé vừa thay đổi. Vui lòng tải lại chi tiết.");
            }
            return;
        }

        if (!PassengerTicketStatus.CHANGED.name().equals(ticketStatus)) {
            throw new BusinessRuleException("Trạng thái vé không hợp lệ để đổi ghế.");
        }
    }

    private void validateHoldSession(String holdToken) {
        if (holdToken == null || holdToken.isBlank()) {
            throw new BusinessRuleException("Thiếu phiên giữ ghế. Vui lòng chọn lại ghế.");
        }
    }

    private void validateAccompaniedChildRequest(StaffPassengerChangePassengerRequest request) {
        if (Boolean.TRUE.equals(request.removeAccompaniedChild()) && request.accompaniedChild() != null) {
            throw new BusinessRuleException("Không thể vừa xóa vừa cập nhật thông tin trẻ em đi kèm.");
        }
    }

    private void syncAccompaniedChild(
        Integer ticketDetailId,
        Integer accountId,
        StaffPassengerChangePassengerRequest request
    ) {
        Optional<AccompaniedChild> existingChild =
            accompaniedChildRepository.findByTicketDetailId(ticketDetailId);

        if (Boolean.TRUE.equals(request.removeAccompaniedChild())) {
            existingChild.ifPresent(accompaniedChildRepository::delete);
            return;
        }

        AccompaniedChildDTO childPayload = request.accompaniedChild();
        if (childPayload == null) {
            return;
        }

        if (existingChild.isPresent()) {
            AccompaniedChild child = existingChild.get();
            child.setFullname(childPayload.fullname().trim());
            child.setBirthYear(childPayload.birthYear());
            child.setUpdatedBy(accountId);
            accompaniedChildRepository.save(child);
            return;
        }

        AccompaniedChild child = AccompaniedChild.builder()
            .ticketDetailId(ticketDetailId)
            .fullname(childPayload.fullname().trim())
            .birthYear(childPayload.birthYear())
            .build();
        child.setCreatedBy(accountId);
        accompaniedChildRepository.save(child);
    }
}
