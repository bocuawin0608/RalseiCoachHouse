package com.ralsei.service;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.dto.response.cargoticket.CustomerContactResponse;

import java.util.List;

public interface CargoTicketService {
    PagedResponse<CargoTicketResponse> getAllCargoTickets(int page, int size);

    CargoTicketFormOptionsResponse getFormOptions(Integer pickupStopId, Integer dropoffStopId);

    CargoTicketResponse getCargoTicketById(int id);

    CargoTicketResponse createCargoTicket(CargoTicketRequest request);

    CargoTicketResponse updateCargoTicket(int id, CargoTicketRequest request);

    void disable(int id);

    List<CustomerContactResponse> searchContacts(String phone);
}
