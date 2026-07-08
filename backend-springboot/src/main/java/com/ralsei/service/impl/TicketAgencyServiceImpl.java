package com.ralsei.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.TicketAgencyListProjection;
import com.ralsei.dto.request.ticketagency.CreateTicketAgencyRequest;
import com.ralsei.dto.request.ticketagency.TicketAgencyFilterRequest;
import com.ralsei.dto.request.ticketagency.UpdateTicketAgencyRequest;
import com.ralsei.dto.response.ticketagency.CoachStopDropdownDTO;
import com.ralsei.dto.response.ticketagency.TicketAgencyDetailResponse;
import com.ralsei.dto.response.ticketagency.TicketAgencyListResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CoachStop;
import com.ralsei.model.TicketAgency;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TicketAgencyRepository;
import com.ralsei.service.TicketAgencyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Implementation of {@link com.ralsei.service.TicketAgencyService}.
 */

public class TicketAgencyServiceImpl implements TicketAgencyService {

    private final TicketAgencyRepository ticketAgencyRepo;
    private final CoachStopRepository coachStopRepo;
    private final StaffRepository staffRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<TicketAgencyListResponse> filterTicketAgencies(TicketAgencyFilterRequest filterRequest, Pageable pageable) {
        String search = filterRequest != null && filterRequest.search() != null && !filterRequest.search().isBlank()
            ? filterRequest.search().trim() : null;
        Boolean isActive = filterRequest != null ? filterRequest.isActive() : null;

        List<TicketAgencyListProjection> projections = ticketAgencyRepo.filterTicketAgencies(search, isActive);

        List<TicketAgencyListResponse> responses = projections.stream()
            .map(this::mapToListResponse)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<TicketAgencyListResponse> pageContent = start < responses.size() ? responses.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketAgencyDetailResponse getTicketAgencyDetail(Integer ticketAgencyId) {
        TicketAgency ta = ticketAgencyRepo.findById(ticketAgencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Bến xe không tồn tại!"));
        return mapToDetailResponse(ta);
    }

    @Override
    @Transactional
    public Integer createTicketAgency(CreateTicketAgencyRequest request) {
        coachStopRepo.findById(request.stopPointId())
            .orElseThrow(() -> new ResourceNotFoundException("Điểm dừng không tồn tại!"));

        TicketAgency ta = TicketAgency.builder()
            .ticketAgencyName(request.ticketAgencyName().trim())
            .stopPointId(request.stopPointId())
            .isActive(true)
            .build();
        return ticketAgencyRepo.save(ta).getTicketAgencyId();
    }

    @Override
    @Transactional
    public void updateTicketAgency(Integer ticketAgencyId, UpdateTicketAgencyRequest request) {
        TicketAgency ta = ticketAgencyRepo.findById(ticketAgencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Bến xe không tồn tại!"));

        coachStopRepo.findById(request.stopPointId())
            .orElseThrow(() -> new ResourceNotFoundException("Điểm dừng không tồn tại!"));

        ta.setTicketAgencyName(request.ticketAgencyName().trim());
        ta.setStopPointId(request.stopPointId());
        if (request.isActive() != null) {
            ta.setActive(request.isActive());
        }
        ticketAgencyRepo.save(ta);
    }

    @Override
    @Transactional
    public void toggleActive(Integer ticketAgencyId) {
        TicketAgency ta = ticketAgencyRepo.findById(ticketAgencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Bến xe không tồn tại!"));
        ta.setActive(!ta.isActive());
        ticketAgencyRepo.save(ta);
    }

    @Override
    @Transactional
    public void deleteTicketAgency(Integer ticketAgencyId) {
        TicketAgency ta = ticketAgencyRepo.findById(ticketAgencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Bến xe không tồn tại!"));
        long staffCount = staffRepo.countByTicketAgencyId(ticketAgencyId);
        if (staffCount > 0) {
            throw new BusinessRuleException(
                "Không thể xóa bến xe đang có " + staffCount + " nhân viên. Vui lòng chuyển nhân viên trước!");
        }
        ticketAgencyRepo.delete(ta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachStopDropdownDTO> getCoachStopDropdown() {
        return coachStopRepo.findAll().stream()
            .filter(CoachStop::isActive)
            .map(cs -> new CoachStopDropdownDTO(cs.getStopPointId(), cs.getStopPointName()))
            .collect(Collectors.toList());
    }

    private TicketAgencyListResponse mapToListResponse(TicketAgencyListProjection proj) {
        return new TicketAgencyListResponse(
            proj.getTicketAgencyId(),
            proj.getTicketAgencyName(),
            proj.getStopPointId(),
            proj.getStopPointName(),
            proj.getCity(),
            proj.getIsActive() != null && proj.getIsActive(),
            proj.getStaffCount() != null ? proj.getStaffCount() : 0L,
            null
        );
    }

    private TicketAgencyDetailResponse mapToDetailResponse(TicketAgency ta) {
        CoachStop cs = coachStopRepo.findById(ta.getStopPointId()).orElse(null);
        String stopPointName = cs != null ? cs.getStopPointName() : null;
        String city = cs != null ? cs.getCity() : null;
        return new TicketAgencyDetailResponse(
            ta.getTicketAgencyId(),
            ta.getTicketAgencyName(),
            ta.getStopPointId(),
            stopPointName,
            city,
            ta.isActive(),
            staffRepo.countByTicketAgencyId(ta.getTicketAgencyId()),
            ta.getCreatedAt(),
            ta.getCreatedBy(),
            ta.getUpdatedAt(),
            ta.getUpdatedBy()
        );
    }
}
