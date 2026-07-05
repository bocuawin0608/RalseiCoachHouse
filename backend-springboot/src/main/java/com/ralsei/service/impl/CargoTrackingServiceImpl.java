package com.ralsei.service.impl;

import com.ralsei.dto.response.CargoTrackingResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.*;
import com.ralsei.repository.*;
import com.ralsei.service.CargoTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CargoTrackingServiceImpl implements CargoTrackingService {

    private final CargoTicketRepository cargoTicketRepository;
    private final CargoTicketDetailRepository cargoTicketDetailRepository;
    private final CoachStopRepository coachStopRepository;
    private final TripRepository tripRepository;
    private final CargoTypePriceRepository cargoTypePriceRepository;

    @Override
    @Transactional(readOnly = true)
    public CargoTrackingResponse trackByCode(String ticketCode) {
        CargoTicket ticket = cargoTicketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + ticketCode));

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
