package com.ralsei.service;

import com.ralsei.dto.response.CargoTrackingResponse;

public interface CargoTrackingService {
    CargoTrackingResponse trackByCode(String ticketCode);
}
