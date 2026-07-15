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

/**
 * Provides the ticket agency service impl component for the application.
 */
public class TicketAgencyServiceImpl implements TicketAgencyService {

    private final TicketAgencyRepository ticketAgencyRepo;
    private final CoachStopRepository coachStopRepo;
    private final StaffRepository staffRepo;

    @Override
    @Transactional(readOnly = true)
    /**
     * Filters the ticket agencies records.
     *
     * @param filterRequest the value supplied for this operation
     * @param pageable the value supplied for this operation
     *
     * @return the filtered results
     */
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
    /**
     * Returns the ticket agency detail.
     *
     * @param ticketAgencyId the value supplied for this operation
     *
     * @return the ticket agency detail
     */
    public TicketAgencyDetailResponse getTicketAgencyDetail(Integer ticketAgencyId) {
        TicketAgency ta = ticketAgencyRepo.findById(ticketAgencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Bến xe không tồn tại!"));
        return mapToDetailResponse(ta);
    }

    @Override
    @Transactional
    /**
     * Creates the ticket agency.
     *
     * @param request the value supplied for this operation
     *
     * @return the created ticket agency
     */
    public Integer createTicketAgency(CreateTicketAgencyRequest request) {
        Integer stopId = request.stopPointId();
        coachStopRepo.findById(stopId)
            .orElseThrow(() -> new ResourceNotFoundException("Điểm dừng không tồn tại!"));

        if (ticketAgencyRepo.existsByStopPointId(stopId)) {
            throw new BusinessRuleException("Điểm dừng này đã có đại lý! Mỗi điểm dừng chỉ được một đại lý.");
        }

        String name = request.ticketAgencyName().trim();
        if (ticketAgencyRepo.existsByTicketAgencyNameIgnoreCaseAndStopPointId(name, stopId)) {
            throw new BusinessRuleException("Đại lý '" + name + "' đã tồn tại tại điểm dừng này!");
        }

        TicketAgency ta = TicketAgency.builder()
            .ticketAgencyName(name)
            .stopPointId(stopId)
            .isActive(true)
            .build();
        return ticketAgencyRepo.save(ta).getTicketAgencyId();
    }

    @Override
    @Transactional
    /**
     * Updates the ticket agency.
     *
     * @param ticketAgencyId the value supplied for this operation
     * @param request the value supplied for this operation
     */
    public void updateTicketAgency(Integer ticketAgencyId, UpdateTicketAgencyRequest request) {
        TicketAgency ta = ticketAgencyRepo.findById(ticketAgencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Bến xe không tồn tại!"));

        String newName = request.ticketAgencyName().trim();
        Integer currentStopId = ta.getStopPointId();

        if (!ta.getTicketAgencyName().equalsIgnoreCase(newName)
            && ticketAgencyRepo.existsByTicketAgencyNameIgnoreCaseAndStopPointId(newName, currentStopId)) {
            throw new BusinessRuleException("Đại lý '" + newName + "' đã tồn tại tại điểm dừng này!");
        }

        ta.setTicketAgencyName(newName);
        if (request.isActive() != null) {
            ta.setActive(request.isActive());
        }
        ticketAgencyRepo.save(ta);
    }

    @Override
    @Transactional
    /**
     * Executes the toggle active operation.
     *
     * @param ticketAgencyId the value supplied for this operation
     */
    public void toggleActive(Integer ticketAgencyId) {
        TicketAgency ta = ticketAgencyRepo.findById(ticketAgencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Bến xe không tồn tại!"));
        if (ta.isActive()) {
            long staffCount = staffRepo.countByTicketAgencyId(ticketAgencyId);
            if (staffCount > 0) {
                throw new BusinessRuleException(
                    "Đại lý này còn " + staffCount + " nhân viên đang làm việc. Vui lòng chuyển họ sang đại lý khác trước khi vô hiệu hóa.");
            }
        }
        ta.setActive(!ta.isActive());
        ticketAgencyRepo.save(ta);
    }

    @Override
    @Transactional
    /**
     * Deletes the ticket agency.
     *
     * @param ticketAgencyId the value supplied for this operation
     */
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
            .map(cs -> new CoachStopDropdownDTO(cs.getStopPointId(), cs.getStopPointName(), cs.getAddress(), cs.getCity()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachStopDropdownDTO> getAvailableStops() {
        return coachStopRepo.findAvailableStops().stream()
            .map(p -> new CoachStopDropdownDTO(p.getStopPointId(), p.getStopPointName(), p.getAddress(), p.getCity()))
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
        String address = cs != null ? cs.getAddress() : null;

        List<TicketAgencyDetailResponse.StaffSummary> staffList = staffRepo.findByTicketAgencyId(ta.getTicketAgencyId())
            .stream()
            .map(s -> new TicketAgencyDetailResponse.StaffSummary(s.getStaffId(), s.getStaffName(), s.getStaffPosition()))
            .collect(Collectors.toList());

        return new TicketAgencyDetailResponse(
            ta.getTicketAgencyId(),
            ta.getTicketAgencyName(),
            ta.getStopPointId(),
            stopPointName,
            city,
            address,
            ta.isActive(),
            staffRepo.countByTicketAgencyId(ta.getTicketAgencyId()),
            staffList,
            ta.getCreatedAt(),
            ta.getCreatedBy(),
            ta.getUpdatedAt(),
            ta.getUpdatedBy()
        );
    }
}
