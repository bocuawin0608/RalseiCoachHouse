package com.ralsei.dto.response.cargoticket;

import java.util.List;

import com.ralsei.dto.projection.cargoticket.CargoTicketCustomerOptionProjection;
import com.ralsei.dto.projection.cargoticket.CargoTicketStaffOptionProjection;
import com.ralsei.dto.projection.cargoticket.CargoTicketStopOptionProjection;
import com.ralsei.dto.projection.cargoticket.CargoTicketTripOptionWithCoachTypeProjection;
import com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
/**
 * Represents the response payload for cargo ticket form options operations.
 */
public class CargoTicketFormOptionsResponse {
    private List<RouteDropdownDTO> routes;
    private List<CargoTicketTripOptionWithCoachTypeProjection> trips;
    private List<CargoTicketCustomerOptionProjection> customers;
    private List<CargoTicketStopOptionProjection> stops;
    private List<CargoTicketStaffOptionProjection> sellers;
    private List<CargoTicketStaffOptionProjection> handlers;
    private List<CargoTicketStaffOptionProjection> drivers;

    /** Authenticated ticket office pickup stop — locked on create/update forms. */
    private Integer agencyPickupStopId;
    private String agencyPickupStopName;
    private String agencyCity;
    /** Outbound route from the agency city that still has later dropoff stops. */
    private Integer defaultRouteId;
    private String defaultRouteName;
}
