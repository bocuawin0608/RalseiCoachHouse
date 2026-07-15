package com.ralsei.dto.projection;

/**
 * TicketAgencyListProjection
 */

/**
 * Projects the ticket agency lis data shape for query results.
 */
public interface TicketAgencyListProjection {
    Integer getTicketAgencyId();
    String getTicketAgencyName();
    Integer getStopPointId();
    String getStopPointName();
    String getCity();
    Boolean getIsActive();
    Long getStaffCount();
}
