package com.ralsei.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.customer.CargoOrderLookupProjection;
import com.ralsei.dto.projection.customer.CargoOrderStopProjection;
import com.ralsei.dto.response.customer.CargoOrderLookupResponse;
import com.ralsei.dto.response.customer.CargoOrderLookupResponse.CargoDetailResponse;
import com.ralsei.dto.response.customer.CargoOrderLookupResponse.CargoPartyResponse;
import com.ralsei.dto.response.customer.CargoOrderLookupResponse.CargoRouteStopResponse;
import com.ralsei.dto.response.customer.CargoOrderLookupResponse.CargoStopResponse;
import com.ralsei.repository.CargoTicketDetailRepository;
import com.ralsei.service.CargoOrderLookupService;

import lombok.RequiredArgsConstructor;

/** Assembles repository rows into stable order-level customer responses. */
@Service
@RequiredArgsConstructor
/**
 * Provides the cargo order lookup service impl component for the application.
 */
public class CargoOrderLookupServiceImpl implements CargoOrderLookupService {
    private final CargoTicketDetailRepository cargoTicketDetailRepository;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    /**
     * Finds the by account id.
     *
     * @param accountId the value supplied for this operation
     *
     * @return the matching result
     */
    public List<CargoOrderLookupResponse> findByAccountId(Integer accountId) {
        validateAccountId(accountId);
        List<CargoOrderLookupProjection> rows = cargoTicketDetailRepository.findCargoOrdersByAccountId(accountId);
        Map<Integer, List<CargoOrderStopProjection>> stopsByOrder = new LinkedHashMap<>();
        if (!rows.isEmpty()) {
            cargoTicketDetailRepository.findCargoOrderStopsByAccountId(accountId).forEach(stop ->
                stopsByOrder.computeIfAbsent(stop.getCargoTicketId(), ignored -> new ArrayList<>()).add(stop));
        }

        Map<Integer, List<CargoOrderLookupProjection>> rowsByOrder = new LinkedHashMap<>();
        rows.forEach(row -> rowsByOrder.computeIfAbsent(row.getCargoTicketId(), ignored -> new ArrayList<>()).add(row));
        return rowsByOrder.values().stream().map(orderRows -> assemble(orderRows, stopsByOrder)).toList();
    }

    /** Rejects tokens that do not contain a usable account ownership claim. */
    private void validateAccountId(Integer accountId) {
        if (accountId == null || accountId < 1) {
            throw new IllegalArgumentException("Không xác định được tài khoản khách hàng.");
        }
    }

    /** Converts one grouped master ticket and its ordered stops into an API response. */
    private CargoOrderLookupResponse assemble(
        List<CargoOrderLookupProjection> rows,
        Map<Integer, List<CargoOrderStopProjection>> stopsByOrder
    ) {
        CargoOrderLookupProjection first = rows.get(0);
        List<CargoDetailResponse> items = rows.stream().map(row -> new CargoDetailResponse(
            row.getCargoTicketDetailId(), row.getCargoTypeName(), row.getUnit(), row.getDetailDescription(),
            row.getQuantity(), row.getWeightKg(), row.getDimensionVol(), row.getCalculatedPrice())).toList();
        List<CargoRouteStopResponse> stops = stopsByOrder.getOrDefault(first.getCargoTicketId(), List.of()).stream()
            .map(stop -> new CargoRouteStopResponse(stop.getStopPointId(), stop.getStopPointName(), stop.getAddress(),
                stop.getCity(), stop.getStopOrder(), stop.getEstimatedStopTime()))
            .toList();

        return new CargoOrderLookupResponse(
            first.getCargoTicketId(), first.getTicketCode(), first.getStatus(), first.getTotalPrice(),
            first.getCodAmount(), first.getFeePayer(), first.getTicketDescription(), first.getBookedAt(),
            first.getTripId(), first.getDepartureTime(), first.getRouteName(), first.getLicensePlate(),
            first.getDriverName(), first.getTicketAgencyName(),
            new CargoStopResponse(first.getPickupStopId(), first.getPickupStopName(), first.getPickupAddress(), first.getPickupCity()),
            new CargoStopResponse(first.getDropoffStopId(), first.getDropoffStopName(), first.getDropoffAddress(), first.getDropoffCity()),
            new CargoPartyResponse(first.getSenderName(), first.getSenderPhone()),
            new CargoPartyResponse(first.getReceiverName(), first.getReceiverPhone()), items, stops);
    }
}
