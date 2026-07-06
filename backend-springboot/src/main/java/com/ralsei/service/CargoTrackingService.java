package com.ralsei.service;

import java.util.List;

import com.ralsei.dto.response.CargoHistoryListResponse;
import com.ralsei.dto.response.CargoTrackingResponse;

/**
 * Service interface for cargo tracking operations.
 */
public interface CargoTrackingService {
    /** Looks up a cargo order by its public ticket code (unauthenticated). */
    CargoTrackingResponse trackByCode(String ticketCode);

    /** Returns cargo orders for the given customer username, optionally filtered by status. */
    List<CargoHistoryListResponse> getMyCargoHistory(String username, String status);

    /** Returns full detail of a specific cargo order. */
    CargoTrackingResponse getCargoDetail(Integer cargoTicketId);
}
