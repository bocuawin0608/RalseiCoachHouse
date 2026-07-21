package com.ralsei.service;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.request.cargoticket.CargoTicketWithDetailsRequest;
import com.ralsei.dto.request.cargoticket.CargoTripAssignRequest;
import com.ralsei.dto.request.cargoticket.ConfirmReceivedRequest;
import com.ralsei.dto.request.cargoticket.ReceiverPaymentMethodRequest;
import com.ralsei.dto.request.cargoticket.TripByStopRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoAssignableBoardResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoOperationalTripResponse;
import com.ralsei.dto.response.cargoticket.CargoOperationalTripPageResponse;
import com.ralsei.dto.response.cargoticket.CargoReceivingTripPageResponse;
import com.ralsei.dto.response.cargoticket.CargoTripAssignResponse;
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
    /**
     * Returns cargo tickets for the authenticated office, optionally restricted
     * to one operational status and one selected trip.
     */
    PagedResponse<CargoTicketResponse> getAllCargoTickets(
            String status, Integer tripId, Integer accountId, int page, int size);

    /** Returns future coaches that are available for new cargo orders. */
    CargoOperationalTripPageResponse getUpcomingOperationalTrips(
            Integer accountId, int page, int size);

    /** Returns unloaded coaches with cargo awaiting this destination office. */
    CargoReceivingTripPageResponse getReceivingTrips(Integer accountId, int page, int size);

    CargoTicketFormOptionsResponse getFormOptions(
            Integer pickupStopId, Integer dropoffStopId, Integer accountId);

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

    /**
     * Confirms destination-office receipt and records the authenticated ticket
     * staff member who completed the hand-off.
     */
    void confirmReceived(int id, Integer accountId, ConfirmReceivedRequest request);

    /**
     * Destination office selects how the receiver will pay (cash or bank QR).
     */
    CargoTicketResponse chooseReceiverPaymentMethod(
            int id, ReceiverPaymentMethodRequest request, Integer accountId);

    List<CustomerContactResponse> searchContacts(String phone);

    void completePayment(int cargoTicketId);

    List<TripByStopResponse> getTripsByStopsInOrder(TripByStopRequest request);

    CargoTicketDetailPriceResponse calculatePrice(CargoTicketDetailPriceRequest request);

    /**
     * Returns unassigned RECEIVED orders that can be attached to the given trip,
     * plus the trip's current cargo occupancy.
     */
    CargoAssignableBoardResponse getAssignableCargo(int tripId, Integer accountId);

    /**
     * Atomically assigns selected waiting orders to one SCHEDULED trip under capacity.
     */
    CargoTripAssignResponse assignCargoToTrip(
            int tripId, CargoTripAssignRequest request, Integer accountId);
}
