/**
 * Implementation of passenger check-in and dashboard operations for trip staff.
 * Handles QR-based and manual check-in workflows with business rule enforcement.
 */
package com.ralsei.service.tripstaff.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.tripstaff.AssignedTripProjection;
import com.ralsei.dto.projection.tripstaff.PassengerBoardingProjection;
import com.ralsei.dto.request.tripstaff.QrCheckInRequest;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.dto.response.tripstaff.AccompaniedChildResponse;
import com.ralsei.dto.response.tripstaff.CheckInResponse;
import com.ralsei.dto.response.tripstaff.TripStaffDashboardResponse;
import com.ralsei.dto.response.tripstaff.TripStaffPassengerResponse;
import com.ralsei.dto.response.tripstaff.TripStaffSeatResponse;
import com.ralsei.dto.response.tripstaff.TripStaffSummaryResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Route;
import com.ralsei.model.Staff;
import com.ralsei.model.Trip;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.repository.AccompaniedChildRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.repository.TripStaffRepository;
import com.ralsei.service.JwtService;
import com.ralsei.service.tripstaff.TripStaffCheckInPolicy;
import com.ralsei.service.tripstaff.TripStaffPassengerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripStaffPassengerServiceImpl implements TripStaffPassengerService {

    private final JwtService jwtService;
    private final StaffRepository staffRepository;
    private final TripStaffRepository tripStaffRepository;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final TripSeatRepository tripSeatRepository;
    private final PassengerTicketRepository passengerTicketRepository;
    private final PassengerTicketDetailRepository passengerTicketDetailRepository;
    private final AccompaniedChildRepository accompaniedChildRepository;
    private final TripStaffCheckInPolicy checkInPolicy;

    private final Set<Integer> noShowTicketDetailIds = Collections.synchronizedSet(new HashSet<>());

    @Override
    @Transactional(readOnly = true)
    public List<AssignedTripProjection> getAssignedTrips(String authorizationHeader, LocalDate date) {
        int staffId = resolveStaffId(authorizationHeader);
        return tripStaffRepository.findAssignedTripsByStaffAndDate(staffId, date.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public TripStaffDashboardResponse getDashboard(String authorizationHeader, Integer tripId) {
        int staffId = resolveStaffId(authorizationHeader);
        assertStaffCanAccessTrip(staffId, tripId);

        Trip trip = tripRepository.findByIdWithRouteAndCoach(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Chuyến đi không tồn tại"));

        List<AssignedTripProjection> assigned = tripStaffRepository.findAssignedTripsByStaffAndDate(
                staffId, trip.getDepartureTime().toLocalDate().toString());
        AssignedTripProjection summary = assigned.stream()
                .filter(item -> item.getTripId().equals(tripId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Bạn không được phân công chuyến này"));

        List<PassengerBoardingProjection> passengerRows = tripStaffRepository.findPassengersForTrip(tripId);
        List<TripStaffPassengerResponse> passengers = passengerRows.stream()
                .map(this::mapPassenger)
                .toList();

        List<TripStaffSeatResponse> seats = buildSeatResponses(tripId, passengerRows);

        Set<Integer> noShowIds = new HashSet<>(noShowTicketDetailIds);

        TripStaffSummaryResponse tripSummary = new TripStaffSummaryResponse(
                summary.getTripId(),
                summary.getRouteName(),
                summary.getDepartureTime(),
                summary.getLicensePlate(),
                summary.getCoachTypeName(),
                summary.getTripStatus(),
                summary.getAssignedRole(),
                summary.getCheckedInCount() != null ? summary.getCheckedInCount() : 0,
                summary.getTotalPassengers() != null ? summary.getTotalPassengers() : 0);

        return new TripStaffDashboardResponse(tripSummary, seats, passengers, noShowIds);
    }

    @Override
    @Transactional
    public CheckInResponse checkInByQr(String authorizationHeader, Integer tripId, QrCheckInRequest request) {
        int staffId = resolveStaffId(authorizationHeader);
        TripContext context = loadTripContext(staffId, tripId);

        PassengerTicketDetail detail = passengerTicketDetailRepository.findByQrcode(request.qrToken().trim())
                .orElseThrow(() -> new BusinessRuleException("Mã QR không hợp lệ"));

        return performCheckIn(context, detail);
    }

    @Override
    @Transactional
    public CheckInResponse checkInManual(String authorizationHeader, Integer tripId, Integer ticketDetailId) {
        int staffId = resolveStaffId(authorizationHeader);
        TripContext context = loadTripContext(staffId, tripId);

        PassengerTicketDetail detail = passengerTicketDetailRepository.findById(ticketDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hành khách trên chuyến này"));

        return performCheckIn(context, detail);
    }

    private CheckInResponse performCheckIn(TripContext context, PassengerTicketDetail detail) {
        PassengerTicket ticket = passengerTicketRepository.findById(detail.getPassengerTicketId())
                .orElseThrow(() -> new BusinessRuleException("Mã QR không hợp lệ"));

        checkInPolicy.assertTicketBelongsToTrip(ticket, context.tripId());
        checkInPolicy.assertTicketConfirmed(ticket);
        checkInPolicy.assertDetailReadyForCheckIn(detail);

        int updated = passengerTicketDetailRepository.updateStatusIfCurrent(
                detail.getTicketDetailId(),
                PassengerTicketDetailStatus.CONFIRMED.name(),
                PassengerTicketDetailStatus.CHECKED_IN.name());

        if (updated == 0) {
            PassengerTicketDetail current = passengerTicketDetailRepository.findById(detail.getTicketDetailId())
                    .orElseThrow(() -> new BusinessRuleException("Mã QR không hợp lệ"));
            checkInPolicy.assertDetailReadyForCheckIn(current);
            throw new BusinessRuleException("Không thể check-in vé này");
        }

        return buildCheckInResponse(detail, ticket);
    }

    private TripContext loadTripContext(int staffId, int tripId) {
        assertStaffCanAccessTrip(staffId, tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Chuyến đi không tồn tại"));

        Route route = routeRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường của chuyến"));

        checkInPolicy.assertStaffAssigned(trip, staffId);
        checkInPolicy.assertWithinCheckInWindow(trip, route, LocalDateTime.now());

        return new TripContext(tripId, trip, route);
    }

    private void assertStaffCanAccessTrip(int staffId, int tripId) {
        if (!tripStaffRepository.isStaffAssignedToTrip(tripId, staffId)) {
            throw new BusinessRuleException("Bạn không được phân công chuyến này");
        }
    }

    private int resolveStaffId(String authorizationHeader) {
        Integer accountId = jwtService.extractAccountId(authorizationHeader);
        if (accountId == null) {
            throw new BusinessRuleException("Không thể xác định tài khoản đăng nhập");
        }

        Staff staff = staffRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessRuleException("Tài khoản chưa liên kết nhân viên"));

        if (!staff.isActive()) {
            throw new BusinessRuleException("Nhân viên không còn hoạt động");
        }

        return staff.getStaffId();
    }

    private List<TripStaffSeatResponse> buildSeatResponses(int tripId, List<PassengerBoardingProjection> passengers) {
        List<TripSeatResponse> seatMap = tripSeatRepository.getSeatMap(tripId);
        Map<String, PassengerBoardingProjection> bySeatCode = new HashMap<>();
        for (PassengerBoardingProjection passenger : passengers) {
            bySeatCode.put(passenger.getSeatCodeSnapshot(), passenger);
        }

        return seatMap.stream()
                .map(seat -> {
                    PassengerBoardingProjection passenger = bySeatCode.get(seat.seatCode());
                    boolean noShow = passenger != null && noShowTicketDetailIds.contains(passenger.getTicketDetailId());
                    return new TripStaffSeatResponse(
                            seat.tripSeatId(),
                            seat.seatCode(),
                            seat.rowIndex(),
                            seat.colIndex(),
                            seat.floorIndex(),
                            seat.status(),
                            passenger != null ? passenger.getStatus() : null,
                            passenger != null ? passenger.getFullName() : null,
                            noShow);
                })
                .toList();
    }

    private TripStaffPassengerResponse mapPassenger(PassengerBoardingProjection row) {
        AccompaniedChildResponse child = null;
        if (row.getChildFullname() != null) {
            child = new AccompaniedChildResponse(row.getChildFullname(), row.getChildBirthYear());
        }
        return new TripStaffPassengerResponse(
                row.getTicketDetailId(),
                row.getFullName(),
                row.getPhone(),
                row.getSeatCodeSnapshot(),
                row.getPickupStopName(),
                row.getDropoffStopName(),
                row.getStatus(),
                child);
    }

    private CheckInResponse buildCheckInResponse(PassengerTicketDetail detail, PassengerTicket ticket) {
        AccompaniedChildResponse child = accompaniedChildRepository.findByTicketDetailId(detail.getTicketDetailId())
                .map(ac -> new AccompaniedChildResponse(ac.getFullname(), ac.getBirthYear()))
                .orElse(null);

        return new CheckInResponse(
                detail.getTicketDetailId(),
                detail.getFullName(),
                detail.getSeatCodeSnapshot(),
                ticket.getPickupStopName(),
                ticket.getDropoffStopName(),
                PassengerTicketDetailStatus.CHECKED_IN.name(),
                child);
    }

    @Override
    @Transactional
    public void startTrip(String authorizationHeader, Integer tripId) {
        int staffId = resolveStaffId(authorizationHeader);
        assertStaffCanAccessTrip(staffId, tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Chuyến đi không tồn tại"));

        if (!"SCHEDULED".equals(trip.getStatus())) {
            throw new BusinessRuleException("Chỉ có thể bắt đầu chuyến ở trạng thái SCHEDULED");
        }

        trip.setStatus("IN_PROGRESS");
        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void endTrip(String authorizationHeader, Integer tripId) {
        int staffId = resolveStaffId(authorizationHeader);
        assertStaffCanAccessTrip(staffId, tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Chuyến đi không tồn tại"));

        if (!"IN_PROGRESS".equals(trip.getStatus())) {
            throw new BusinessRuleException("Chỉ có thể kết thúc chuyến ở trạng thái IN_PROGRESS");
        }

        trip.setStatus("COMPLETED");
        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void markNoShow(String authorizationHeader, Integer tripId, Integer ticketDetailId) {
        int staffId = resolveStaffId(authorizationHeader);
        assertStaffCanAccessTrip(staffId, tripId);

        PassengerTicketDetail detail = passengerTicketDetailRepository.findById(ticketDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hành khách"));

        PassengerTicket ticket = passengerTicketRepository.findById(detail.getPassengerTicketId())
                .orElseThrow(() -> new BusinessRuleException("Không tìm thấy vé"));

        checkInPolicy.assertTicketBelongsToTrip(ticket, tripId);
        checkInPolicy.assertTicketConfirmed(ticket);

        if (!"CONFIRMED".equals(detail.getStatus())) {
            throw new BusinessRuleException("Chỉ có thể đánh dấu vắng mặt cho hành khách chưa check-in");
        }

        noShowTicketDetailIds.add(ticketDetailId);
    }

    private record TripContext(int tripId, Trip trip, Route route) {}
}
