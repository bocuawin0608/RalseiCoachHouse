package com.ralsei.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.ticketagency.CreateTicketAgencyRequest;
import com.ralsei.dto.request.ticketagency.TicketAgencyFilterRequest;
import com.ralsei.dto.request.ticketagency.UpdateTicketAgencyRequest;
import com.ralsei.dto.response.ticketagency.CoachStopDropdownDTO;
import com.ralsei.dto.response.ticketagency.TicketAgencyDetailResponse;
import com.ralsei.dto.response.ticketagency.TicketAgencyListResponse;

/**
 * Service interface for ticketagency management operations.
 */

public interface TicketAgencyService {
    Page<TicketAgencyListResponse> filterTicketAgencies(TicketAgencyFilterRequest filterRequest, Pageable pageable);
    TicketAgencyDetailResponse getTicketAgencyDetail(Integer ticketAgencyId);
    Integer createTicketAgency(CreateTicketAgencyRequest request);
    void updateTicketAgency(Integer ticketAgencyId, UpdateTicketAgencyRequest request);
    void toggleActive(Integer ticketAgencyId);
    void deleteTicketAgency(Integer ticketAgencyId);
    List<CoachStopDropdownDTO> getCoachStopDropdown();
    List<CoachStopDropdownDTO> getAvailableStops();
}
