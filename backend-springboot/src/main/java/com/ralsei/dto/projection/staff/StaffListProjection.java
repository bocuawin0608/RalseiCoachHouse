package com.ralsei.dto.projection.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface StaffListProjection {
    Integer getStaffId();
    String getStaffName();
    String getPhone();
    String getEmail();
    String getCccd();
    String getStaffPosition();
    Integer getTicketAgencyId();
    String getTicketAgencyName();
    String getUsername();
    LocalDate getDob();
    LocalDate getHireDate();
    LocalDateTime getCreatedAt();
    Boolean getIsActive();
}
