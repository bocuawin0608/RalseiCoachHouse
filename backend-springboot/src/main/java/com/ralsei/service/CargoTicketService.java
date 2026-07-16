package com.ralsei.service;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.request.cargoticket.CargoTicketWithDetailsRequest;
import com.ralsei.dto.request.cargoticket.TripByStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoOperationalTripResponse;
import com.ralsei.dto.response.cargoticket.CargoOperationalTripPageResponse;
import com.ralsei.dto.response.cargoticket.CustomerContactResponse;
import com.ralsei.dto.request.cargoticketdetail.CargoTicketDetailRequest;
import com.ralsei.dto.response.cargoticket.TripByStopResponse;
import com.ralsei.dto.response.cargoticketdetail.CargoTicketDetailPriceResponse;
import com.ralsei.dto.response.cargoticketdetail.CargoTicketDetailResponse;
import com.ralsei.dto.request.cargoticketdetail.CargoTicketDetailPriceRequest;
import java.util.List;

/**
 * Provides the business service contract for cargo ticket.
 */
public interface CargoTicketService {
    /** Returns cargo tickets, optionally restricted to one operational status. */
    PagedResponse<CargoTicketResponse> getAllCargoTickets(String status, Integer accountId, int page, int size);

    /** Returns future coaches that are available for new cargo orders. */
    CargoOperationalTripPageResponse getUpcomingOperationalTrips(
            Integer accountId, int page, int size);

    CargoTicketFormOptionsResponse getFormOptions(Integer pickupStopId, Integer dropoffStopId);

    CargoTicketResponse getCargoTicketById(int id);

    CargoTicketResponse createCargoTicket(CargoTicketRequest request, Integer accountId);

    CargoTicketResponse createCargoTicketWithDetails(CargoTicketWithDetailsRequest request, Integer accountId);

    CargoTicketResponse updateCargoTicket(int id, CargoTicketRequest request);

    /** Atomically updates a pending order and its complete detail collection. */
    CargoTicketResponse updateCargoTicketWithDetails(int id, CargoTicketWithDetailsRequest request);

    List<CargoTicketDetailResponse> getCargoTicketDetailsByTicketId(int cargoTicketId);

    CargoTicketDetailResponse createCargoTicketDetail(int ticketId, CargoTicketDetailRequest request);

    CargoTicketDetailResponse updateCargoTicketDetail(int detailId, CargoTicketDetailRequest request);

    void deleteCargoTicketDetail(int detailId);

    void disable(int id);

    /** Confirms that an arrived package was handed to its receiver. */
    void confirmReceived(int id);

    List<CustomerContactResponse> searchContacts(String phone);

    void completePayment(int cargoTicketId);

    List<TripByStopResponse> getTripsByStopsInOrder(TripByStopRequest request);

    CargoTicketDetailPriceResponse calculatePrice(CargoTicketDetailPriceRequest request);
}
