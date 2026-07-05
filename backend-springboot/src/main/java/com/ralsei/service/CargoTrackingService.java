package com.ralsei.service;

import java.util.List;

import com.ralsei.dto.response.CargoHistoryListResponse;
import com.ralsei.dto.response.CargoTrackingResponse;

public interface CargoTrackingService {
    CargoTrackingResponse trackByCode(String ticketCode);
    List<CargoHistoryListResponse> getMyCargoHistory(String username, String status);
    CargoTrackingResponse getCargoDetail(Integer cargoTicketId);
}
