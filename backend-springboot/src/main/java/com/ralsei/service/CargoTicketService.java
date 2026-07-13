package com.ralsei.service;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.dto.response.cargoticket.CustomerContactResponse;
import com.ralsei.dto.request.cargoticket.TripByStopRequest;
import com.ralsei.dto.response.cargoticket.TripByStopResponse;
import com.ralsei.model.Trip;

import java.util.List;

public interface CargoTicketService {
    PagedResponse<CargoTicketResponse> getAllCargoTickets(int page, int size);

    CargoTicketFormOptionsResponse getFormOptions(Integer pickupStopId, Integer dropoffStopId);

    CargoTicketResponse getCargoTicketById(int id);

    CargoTicketResponse createCargoTicket(CargoTicketRequest request);

    CargoTicketResponse updateCargoTicket(int id, CargoTicketRequest request);

    void disable(int id);

    List<CustomerContactResponse> searchContacts(String phone);

    void completePayment(int cargoTicketId);

    List<TripByStopResponse> getTripsByStopsInOrder(TripByStopRequest request);
}
