package com.ralsei.dto.projection;

/**
 * TicketAgencyListProjection
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
