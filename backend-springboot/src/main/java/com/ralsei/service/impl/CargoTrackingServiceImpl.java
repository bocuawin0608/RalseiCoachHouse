package com.ralsei.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.CargoHistoryListProjection;
import com.ralsei.dto.response.CargoHistoryListResponse;
import com.ralsei.dto.response.CargoTrackingResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Account;
import com.ralsei.model.CargoTicket;
import com.ralsei.model.CargoTicketDetail;
import com.ralsei.model.CoachStop;
import com.ralsei.model.CargoTypePrice;
import com.ralsei.model.Customer;
import com.ralsei.model.Trip;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.CargoTicketDetailRepository;
import com.ralsei.repository.CargoTicketRepository;
import com.ralsei.repository.CargoTypePriceRepository;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.CargoTrackingService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of cargo tracking operations.
 */
@Service
@RequiredArgsConstructor
public class CargoTrackingServiceImpl implements CargoTrackingService {

    private final CargoTicketRepository cargoTicketRepository;
    private final CargoTicketDetailRepository cargoTicketDetailRepository;
    private final CoachStopRepository coachStopRepository;
    private final TripRepository tripRepository;
    private final CargoTypePriceRepository cargoTypePriceRepository;
    private final AccountRepository accountRepo;
    private final CustomerRepository customerRepo;

    @Override
    @Transactional(readOnly = true)
    public CargoTrackingResponse trackByCode(String ticketCode) {
        CargoTicket ticket = cargoTicketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + ticketCode));
        return buildTrackingResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoHistoryListResponse> getMyCargoHistory(String username, String status) {
        Account account = accountRepo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));
        Customer customer = customerRepo.findByAccountId(account.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng!"));

        String statusFilter = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;

        List<CargoHistoryListProjection> projections = cargoTicketRepository
                .findHistoryByCustomerIdOrReceiverPhone(customer.getCustomerId(), customer.getPhone(), statusFilter);

        return projections.stream()
                .map(p -> new CargoHistoryListResponse(
                    p.getCargoTicketId(),
                    p.getTicketCode(),
                    p.getStatus(),
                    p.getSenderName(),
                    p.getSenderPhone(),
                    p.getReceiverName(),
                    p.getReceiverPhone(),
                    p.getTotalPrice(),
                    p.getCreatedAt(),
                    p.getPickupStopName(),
                    p.getDropoffStopName(),
                    p.getTripRouteName(),
                    p.getTripDepartureTime()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CargoTrackingResponse getCargoDetail(Integer cargoTicketId) {
        CargoTicket ticket = cargoTicketRepository.findById(cargoTicketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));
        return buildTrackingResponse(ticket);
    }

    private CargoTrackingResponse buildTrackingResponse(CargoTicket ticket) {
        String pickupStopName = coachStopRepository.findById(ticket.getPickupStopId())
                .map(CoachStop::getStopPointName)
                .orElse("N/A");

        String dropoffStopName = coachStopRepository.findById(ticket.getDropoffStopId())
                .map(CoachStop::getStopPointName)
                .orElse("N/A");

        String tripRouteName = null;
        java.time.LocalDateTime tripDepartureTime = null;
        if (ticket.getTripId() > 0) {
            Trip trip = tripRepository.findById(ticket.getTripId()).orElse(null);
            if (trip != null) {
                tripDepartureTime = trip.getDepartureTime();
                if (trip.getRoute() != null) {
                    tripRouteName = trip.getRoute().getRouteName();
                }
            }
        }

        List<CargoTrackingResponse.CargoDetailItem> items = cargoTicketDetailRepository
                .findByCargoTicketId(ticket.getCargoTicketId())
                .stream()
                .map(this::toDetailItem)
                .collect(Collectors.toList());

        return CargoTrackingResponse.builder()
                .ticketCode(ticket.getTicketCode())
                .status(ticket.getStatus())
                .senderName(ticket.getSenderName())
                .senderPhone(ticket.getSenderPhone())
                .receiverName(ticket.getReceiverName())
                .receiverPhone(ticket.getReceiverPhone())
                .pickupStopName(pickupStopName)
                .dropoffStopName(dropoffStopName)
                .totalPrice(ticket.getTotalPrice())
                .feePayer(ticket.getFeePayer())
                .codAmount(ticket.getCodAmount())
                .description(ticket.getDescription())
                .tripRouteName(tripRouteName)
                .tripDepartureTime(tripDepartureTime)
                .items(items)
                .build();
    }

    private CargoTrackingResponse.CargoDetailItem toDetailItem(CargoTicketDetail detail) {
        String unit = cargoTypePriceRepository.findById(detail.getCargoTypePriceId())
                .map(CargoTypePrice::getUnit)
                .orElse(null);

        return CargoTrackingResponse.CargoDetailItem.builder()
                .description(detail.getDescription())
                .quantity(detail.getQuantity())
                .weightKg(detail.getWeightKg())
                .dimensionVol(detail.getDimensionVol())
                .calculatedPrice(detail.getCalculatedPrice())
                .unit(unit)
                .build();
    }
}
