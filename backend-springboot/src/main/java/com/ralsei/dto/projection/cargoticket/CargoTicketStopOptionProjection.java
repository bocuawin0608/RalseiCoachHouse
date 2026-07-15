package com.ralsei.dto.projection.cargoticket;

/**
 * Projects the cargo ticket stop optio data shape for query results.
 */
public interface CargoTicketStopOptionProjection {
    Integer getStopPointId();
    String getStopPointName();
    Integer getRouteId();
    String getCity();
}
