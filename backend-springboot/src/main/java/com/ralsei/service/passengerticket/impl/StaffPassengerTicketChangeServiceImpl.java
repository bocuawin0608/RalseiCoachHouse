package com.ralsei.service.passengerticket.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
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
import com.ralsei.dto.projection.staffpassengerticket.StaffPassengerTransferCandidateProjection;
import com.ralsei.dto.request.staffpassengerticket.StaffPassengerItineraryChangeRequest;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerItineraryPreviewResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTransferCandidateResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.AccompaniedChild;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.RouteStop;
import com.ralsei.model.Trip;
import com.ralsei.model.TripSeat;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketMajorChangeType;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.repository.AccompaniedChildRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.passengerbooking.SeatHoldService;
import com.ralsei.service.passengerticket.PassengerTicketStaffPolicy;
import com.ralsei.service.passengerticket.StaffPassengerTicketChangeService;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;
import com.ralsei.service.passengerticket.StaffPassengerTicketQueryService;
import com.ralsei.service.passengerticket.StaffTicketItineraryPriceCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffPassengerTicketChangeServiceImpl implements StaffPassengerTicketChangeService {

    private static final long STAFF_SEAT_HOLD_TTL_SECONDS = 300;
    private static final String LOCK_MODE_CHANGE_SEAT = "CHANGE_SEAT";
    private static final String LOCK_MODE_ITINERARY = "ITINERARY";

    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final PassengerTicketRepository ticketRepository;
    private final AccompaniedChildRepository accompaniedChildRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RouteStopRepository routeStopRepository;
    private final StaffRepository staffRepository;
    private final SeatHoldService seatHoldService;
    private final PassengerTicketStaffPolicy policy;
    private final StaffPassengerTicketQueryService queryService;
    private final StaffTicketItineraryPriceCalculator itineraryPriceCalculator;

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
    public SeatLockResponse lockSeats(Integer tripId, SeatLockRequest request, String holdToken, String lockMode) {
        validateHoldSession(holdToken);

        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId);
        }

        String normalizedLockMode = lockMode == null ? LOCK_MODE_CHANGE_SEAT : lockMode.trim().toUpperCase();
        boolean itineraryMode = LOCK_MODE_ITINERARY.equals(normalizedLockMode);

        if (!itineraryMode && request.tripSeatIds().size() != 1) {
            throw new BusinessRuleException("Chỉ được giữ một ghế khi đổi chỗ.");
        }

        if (itineraryMode && request.tripSeatIds().isEmpty()) {
            throw new BusinessRuleException("Phải chọn ít nhất một ghế.");
        }

        if (new HashSet<>(request.tripSeatIds()).size() != request.tripSeatIds().size()) {
            throw new BusinessRuleException("Không được chọn trùng ghế.");
        }

        List<Integer> availableSeatIds = tripSeatRepository.findTripSeatIdsByTripIdAndStatus(
            tripId, TripSeatStatus.AVAILABLE
        );

        for (Integer tripSeatId : request.tripSeatIds()) {
            if (!availableSeatIds.contains(tripSeatId)) {
                throw new BusinessRuleException("Ghế đã được đặt hoặc không khả dụng. Vui lòng chọn ghế khác.");
            }
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

    @Override
    @Transactional(readOnly = true)
    public List<StaffPassengerTransferCandidateResponse> getTransferCandidates(
        String ticketCode,
        LocalDate departureDate,
        Integer routeId,
        boolean excludeCurrentTrip
    ) {
        if (departureDate == null) {
            throw new BusinessRuleException("Vui lòng chọn ngày khởi hành.");
        }
        if (routeId == null || routeId < 1) {
            throw new BusinessRuleException("Vui lòng chọn tuyến đường.");
        }

        String normalizedTicketCode = ticketCode == null ? "" : ticketCode.trim();
        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByTicketCode(normalizedTicketCode);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy vé.");
        }

        StaffPassengerTicketRowProjection header = rows.get(0);
        int confirmedSeats = countConfirmedSeats(rows);
        Integer excludeTripId = excludeCurrentTrip ? header.getTripId() : null;

        LocalDateTime dayStart = departureDate.atStartOfDay();
        LocalDateTime dayEnd = departureDate.atTime(23, 59, 59, 999_000_000);
        LocalDateTime minDepartureTime = LocalDateTime.now().plusHours(3);

        return tripRepository.findStaffTransferCandidates(
            routeId,
            dayStart,
            dayEnd,
            minDepartureTime,
            excludeTripId,
            confirmedSeats
        ).stream()
            .map(this::toTransferCandidateResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffPassengerItineraryPreviewResponse previewItineraryChange(
        String ticketCode,
        Integer newTripId,
        Integer pickupStopId,
        Integer dropoffStopId,
        List<Integer> newTripSeatIds
    ) {
        ItineraryContext context = buildItineraryContext(
            ticketCode, newTripId, pickupStopId, dropoffStopId, newTripSeatIds, false
        );

        return buildPreviewResponse(context);
    }

    @Override
    @Transactional
    public StaffPassengerTicketDetailResponse changeItinerary(
        Integer accountId,
        String ticketCode,
        StaffPassengerItineraryChangeRequest request,
        String holdToken
    ) {
        staffRepository.findByAccountId(accountId)
            .orElseThrow(() -> new BusinessRuleException("Không tìm thấy thông tin nhân viên!"));

        ItineraryContext context = buildItineraryContext(
            ticketCode,
            request.newTripId(),
            request.pickupStopId(),
            request.dropoffStopId(),
            request.newTripSeatIds(),
            true
        );

        StaffPassengerItineraryPreviewResponse preview = buildPreviewResponse(context);
        if (!preview.eligible()) {
            throw new BusinessRuleException(
                preview.ineligibleReason() != null
                    ? preview.ineligibleReason()
                    : "Không đủ điều kiện đổi hành trình."
            );
        }

        if (isItineraryUnchanged(context)) {
            return queryService.getDetail(context.normalizedTicketCode());
        }

        if (context.sameTrip()) {
            applySameTripItineraryChange(context, accountId);
        } else {
            validateHoldSession(holdToken);
            applyDifferentTripItineraryChange(context, accountId, holdToken);
        }

        return queryService.getDetail(context.normalizedTicketCode());
    }

    private boolean isItineraryUnchanged(ItineraryContext context) {
        PassengerTicket ticket = context.ticket();
        int newPickupStopId = context.pickupStop().getCoachStop().getStopPointId();
        int newDropoffStopId = context.dropoffStop().getCoachStop().getStopPointId();

        if (context.sameTrip()) {
            return ticket.getPickupStopId() == newPickupStopId
                && ticket.getDropoffStopId() == newDropoffStopId;
        }

        return ticket.getTripId() == context.targetTripId()
            && ticket.getPickupStopId() == newPickupStopId
            && ticket.getDropoffStopId() == newDropoffStopId;
    }

    private void applySameTripItineraryChange(ItineraryContext context, Integer accountId) {
        PassengerTicket ticket = context.ticket();
        RouteStop pickup = context.pickupStop();
        RouteStop dropoff = context.dropoffStop();

        ticket.setPickupStopId(pickup.getCoachStop().getStopPointId());
        ticket.setPickupStopName(pickup.getCoachStop().getStopPointName());
        ticket.setDropoffStopId(dropoff.getCoachStop().getStopPointId());
        ticket.setDropoffStopName(dropoff.getCoachStop().getStopPointName());
        ticket.setUpdatedBy(accountId);
        ticketRepository.save(ticket);

        markTicketChangedIfNeeded(
            context.header().getPassengerTicketId(),
            context.header().getTicketStatus(),
            accountId
        );
    }

    private void applyDifferentTripItineraryChange(
        ItineraryContext context,
        Integer accountId,
        String holdToken
    ) {
        List<Integer> lockedSeatIds = seatHoldService.getTripSeatIdsByToken(holdToken);
        List<Integer> requestedSeatIds = context.requestedSeatIds();

        if (lockedSeatIds.size() != requestedSeatIds.size()
            || !new HashSet<>(lockedSeatIds).containsAll(requestedSeatIds)) {
            throw new BusinessRuleException("Phiên giữ ghế không hợp lệ hoặc đã hết hạn. Vui lòng chọn lại ghế.");
        }

        List<TripSeat> newSeats = tripSeatRepository.findByTripIdAndTripSeatIdInWithSeat(
            context.targetTripId(),
            requestedSeatIds
        );

        if (newSeats.size() != requestedSeatIds.size()) {
            throw new BusinessRuleException("Có ghế không thuộc chuyến mới. Vui lòng chọn lại.");
        }

        if (!newSeats.stream().allMatch(seat -> seat.getStatus() == TripSeatStatus.AVAILABLE)) {
            throw new BusinessRuleException("Có ghế mới không còn trống. Vui lòng chọn lại.");
        }

        newSeats.sort(Comparator.comparing(TripSeat::getTripSeatId));
        List<Integer> sortedRequestedSeatIds = requestedSeatIds.stream().sorted().toList();
        List<TripSeat> orderedNewSeats = sortedRequestedSeatIds.stream()
            .map(id -> newSeats.stream().filter(seat -> seat.getTripSeatId() == id).findFirst()
                .orElseThrow(() -> new BusinessRuleException("Có ghế không hợp lệ.")))
            .toList();

        List<PassengerTicketDetail> confirmedDetails = context.confirmedDetails();
        List<Integer> oldTripSeatIds = confirmedDetails.stream()
            .map(PassengerTicketDetail::getTripSeatId)
            .filter(id -> id != null && id > 0)
            .toList();

        if (!oldTripSeatIds.isEmpty()) {
            tripSeatRepository.updateStatusByTripSeatIds(oldTripSeatIds, TripSeatStatus.AVAILABLE);
        }

        tripSeatRepository.updateStatusByTripSeatIds(requestedSeatIds, TripSeatStatus.SOLD);

        for (int index = 0; index < confirmedDetails.size(); index++) {
            PassengerTicketDetail detail = confirmedDetails.get(index);
            TripSeat newSeat = orderedNewSeats.get(index);
            detail.setTripSeatId(newSeat.getTripSeatId());
            detail.setSeatCodeSnapshot(newSeat.getSeat().getSeatCode());
            detail.setUpdatedBy(accountId);
        }
        ticketDetailRepository.saveAll(confirmedDetails);

        PassengerTicket ticket = context.ticket();
        RouteStop pickup = context.pickupStop();
        RouteStop dropoff = context.dropoffStop();

        ticket.setTripId(context.targetTripId());
        ticket.setPickupStopId(pickup.getCoachStop().getStopPointId());
        ticket.setPickupStopName(pickup.getCoachStop().getStopPointName());
        ticket.setDropoffStopId(dropoff.getCoachStop().getStopPointId());
        ticket.setDropoffStopName(dropoff.getCoachStop().getStopPointName());
        ticket.setUpdatedBy(accountId);
        ticketRepository.save(ticket);

        markTicketChangedIfNeeded(
            context.header().getPassengerTicketId(),
            context.header().getTicketStatus(),
            accountId
        );

        int marked = ticketRepository.markMajorChangeIfUnused(
            context.header().getPassengerTicketId(),
            PassengerTicketMajorChangeType.TRANSFER_TRIP
        );
        if (marked != 1) {
            throw new BusinessRuleException("Vé đã sử dụng quyền đổi chuyến hoặc hủy vé.");
        }
        ticket.setMajorChangeType(PassengerTicketMajorChangeType.TRANSFER_TRIP);

        seatHoldService.releaseSeats(requestedSeatIds, holdToken);
    }

    private ItineraryContext buildItineraryContext(
        String ticketCode,
        Integer newTripId,
        Integer pickupStopId,
        Integer dropoffStopId,
        List<Integer> newTripSeatIds,
        boolean enforcePolicy
    ) {
        String normalizedTicketCode = ticketCode == null ? "" : ticketCode.trim();
        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByTicketCode(normalizedTicketCode);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy vé.");
        }

        StaffPassengerTicketRowProjection header = rows.get(0);
        Trip currentTrip = tripRepository.findById(header.getTripId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe hiện tại."));

        if (enforcePolicy) {
            policy.assertItineraryChangeAllowed(
                header.getTicketStatus(),
                rows,
                header.getPaymentStatus(),
                header.getDepartureTime(),
                currentTrip.getStatus()
            );
        }

        int targetTripId = newTripId != null ? newTripId : header.getTripId();
        boolean sameTrip = targetTripId == header.getTripId();

        Trip targetTrip = sameTrip
            ? currentTrip
            : tripRepository.findById(targetTripId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe mới."));

        List<PassengerTicketDetail> confirmedDetails = ticketDetailRepository
            .findByPassengerTicketId(header.getPassengerTicketId()).stream()
            .filter(detail -> PassengerTicketDetailStatus.CONFIRMED.name().equals(detail.getStatus()))
            .sorted(Comparator.comparing(PassengerTicketDetail::getTicketDetailId))
            .toList();

        int confirmedSeatCount = confirmedDetails.size();
        List<Integer> requestedSeatIds = newTripSeatIds == null
            ? List.of()
            : newTripSeatIds.stream().distinct().sorted().toList();

        PassengerTicket ticket = ticketRepository.findById(header.getPassengerTicketId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé."));

        if (!sameTrip) {
            if (enforcePolicy) {
                int availableSeats = tripSeatRepository.findTripSeatIdsByTripIdAndStatus(
                    targetTripId, TripSeatStatus.AVAILABLE
                ).size();
                policy.assertTransferTripEligible(
                    targetTrip.getDepartureTime(),
                    targetTrip.getStatus(),
                    availableSeats,
                    confirmedSeatCount,
                    ticket.getMajorChangeType()
                );
            }

            if (requestedSeatIds.size() != confirmedSeatCount) {
                throw new BusinessRuleException(
                    "Phải chọn đúng " + confirmedSeatCount + " ghế trên chuyến mới."
                );
            }
        } else if (!requestedSeatIds.isEmpty()) {
            throw new BusinessRuleException("Không cần chọn ghế khi giữ nguyên chuyến.");
        }

        RouteStop pickup = routeStopRepository.findByRouteIdAndStopPointId(
            targetTrip.getRouteId(), pickupStopId
        ).orElseThrow(() -> new BusinessRuleException("Điểm đón không hợp lệ trên tuyến này."));

        RouteStop dropoff = routeStopRepository.findByRouteIdAndStopPointId(
            targetTrip.getRouteId(), dropoffStopId
        ).orElseThrow(() -> new BusinessRuleException("Điểm trả không hợp lệ trên tuyến này."));

        if (pickup.getStopOrder() >= dropoff.getStopOrder()) {
            throw new BusinessRuleException("Lộ trình không hợp lệ: điểm đón phải nằm trước điểm trả.");
        }

        return new ItineraryContext(
            normalizedTicketCode,
            header,
            ticket,
            targetTripId,
            sameTrip,
            confirmedDetails,
            requestedSeatIds,
            pickup,
            dropoff,
            confirmedSeatCount
        );
    }

    private StaffPassengerItineraryPreviewResponse buildPreviewResponse(ItineraryContext context) {
        BigDecimal originalNetPaid = context.header().getPaymentAmount();
        StaffTicketItineraryPriceCalculator.PriceBreakdown breakdown;

        try {
            breakdown = itineraryPriceCalculator.calculateNetPaid(
                context.targetTripId(),
                context.pickupStop().getCoachStop().getStopPointId(),
                context.dropoffStop().getCoachStop().getStopPointId(),
                context.confirmedSeatCount(),
                context.ticket().getVoucherId()
            );
        } catch (BusinessRuleException ex) {
            return new StaffPassengerItineraryPreviewResponse(
                originalNetPaid,
                null,
                false,
                ex.getMessage(),
                !context.sameTrip(),
                context.sameTrip()
            );
        }

        BigDecimal newNetPaid = breakdown.netPaid();
        boolean eligible = originalNetPaid != null && newNetPaid.compareTo(originalNetPaid) <= 0;
        String ineligibleReason = eligible
            ? null
            : "Giá vé mới cao hơn giá đã thanh toán. Chỉ được đổi sang lựa chọn có giá bằng hoặc thấp hơn.";

        return new StaffPassengerItineraryPreviewResponse(
            originalNetPaid,
            newNetPaid,
            eligible,
            ineligibleReason,
            !context.sameTrip(),
            context.sameTrip()
        );
    }

    private StaffPassengerTransferCandidateResponse toTransferCandidateResponse(
        StaffPassengerTransferCandidateProjection projection
    ) {
        return new StaffPassengerTransferCandidateResponse(
            projection.getTripId(),
            projection.getRouteName(),
            projection.getCoachTypeName(),
            projection.getDepartureTime(),
            projection.getSeatPrice(),
            projection.getAvailableSeats(),
            projection.getTotalSeats()
        );
    }

    private int countConfirmedSeats(List<StaffPassengerTicketRowProjection> rows) {
        return (int) rows.stream()
            .filter(row -> PassengerTicketDetailStatus.CONFIRMED.name().equals(row.getDetailStatus()))
            .count();
    }

    private record ItineraryContext(
        String normalizedTicketCode,
        StaffPassengerTicketRowProjection header,
        PassengerTicket ticket,
        int targetTripId,
        boolean sameTrip,
        List<PassengerTicketDetail> confirmedDetails,
        List<Integer> requestedSeatIds,
        RouteStop pickupStop,
        RouteStop dropoffStop,
        int confirmedSeatCount
    ) {}

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
