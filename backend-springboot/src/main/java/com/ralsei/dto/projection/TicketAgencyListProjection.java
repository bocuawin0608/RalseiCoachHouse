package com.ralsei.dto.projection;

public interface TicketAgencyListProjection {
    Integer getTicketAgencyId();
    String getTicketAgencyName();
    Integer getStopPointId();
    String getStopPointName();
    Boolean getIsActive();
    Long getStaffCount();
}
