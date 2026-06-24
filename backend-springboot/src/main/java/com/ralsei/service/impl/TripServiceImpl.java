package com.ralsei.service.impl;

import com.ralsei.dto.projection.coach.CoachLicensePlateProjection;
import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.request.trip.TripCreateRequest;
import com.ralsei.dto.request.trip.TripUpdateRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO;
import com.ralsei.model.Trip;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.TripService;
import com.ralsei.util.FormatHandlerUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TripService.class);
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final StaffRepository staffRepository;
    @Override
    public String insertTrip(TripCreateRequest tripRequest) {
        String prompt = "";
        if (tripRequest.getDepartureTime() == null
                || tripRequest.getDepartureTime().isBefore(java.time.LocalDateTime.now())) {
            LOGGER.error(" Validation Failed: Mẹ mày ko hợp lệ hoặc ở quá khứ.");
            prompt = "Không thể tạo chuyến xe trong quá khứ, vui lòng tạo lại.";
        } else {
            try {
                tripRepository.insertTrip(tripRequest.getRouteId(), tripRequest.getCoachId(),
                        tripRequest.getDepartureTime(), tripRequest.getStatus(), tripRequest.getDriverId(),
                        tripRequest.getAttendantId());

                LOGGER.info("Tạo chuyến xe mới thành công. ID: ");
                prompt = "Tạo chuyến xe mới thành công";
            } catch (Exception e) {
                LOGGER.error("ERROR: Không thể ghi đè mẹ thằng hiếu xuống Database. Lý do: {}", e.getMessage());
                prompt = "Lỗi hệ thống, tạo chuyến thất bại!";
            }
        }
        return prompt;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripDetailProjection> getTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            int page,
            int size) {
        route = FormatHandlerUtility.formatProvinceName(route);
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Your mom are in the past, please check your date range!");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<TripDetailProjection> tripPage = tripRepository.findTripDetails(start, end, route, pageable);

        return new PagedResponse<>(
                tripPage.getContent(),
                tripPage.getNumber(),
                tripPage.getSize(),
                tripPage.getTotalElements(),
                tripPage.getTotalPages(),
                tripPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripFilterProjection> getFilteredTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            List<String> timeSlots,
            List<String> layouts,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        int checkTimeSlots = (timeSlots != null && !timeSlots.isEmpty()) ? 1 : 0;

        String slot1Start = null, slot1End = null;
        String slot2Start = null, slot2End = null;
        String slot3Start = null, slot3End = null;
        String slot4Start = null, slot4End = null;

        if (checkTimeSlots == 1) {
            for (int i = 0; i < Math.min(timeSlots.size(), 4); i++) {
                String slot = timeSlots.get(i);
                if (slot != null && slot.contains("-")) {
                    String[] parts = slot.split("-");
                    String sTime = parts[0].trim();
                    String eTime = parts[1].trim();
                    if (sTime.length() == 5)
                        sTime += ":00";
                    if (eTime.length() == 5)
                        eTime += ":00";

                    switch (i) {
                        case 0 -> {
                            slot1Start = sTime;
                            slot1End = eTime;
                        }
                        case 1 -> {
                            slot2Start = sTime;
                            slot2End = eTime;
                        }
                        case 2 -> {
                            slot3Start = sTime;
                            slot3End = eTime;
                        }
                        case 3 -> {
                            slot4Start = sTime;
                            slot4End = eTime;
                        }
                    }
                }
            }
        }

        int checkLayouts = (layouts != null && !layouts.isEmpty()) ? 1 : 0;
        String layoutKeyword1 = null;
        String layoutKeyword2 = null;

        if (checkLayouts == 1) {
            if (layouts.size() > 0)
                layoutKeyword1 = "%" + layouts.get(0).trim() + "%";
            if (layouts.size() > 1)
                layoutKeyword2 = "%" + layouts.get(1).trim() + "%";
        }

        // 3. Khởi tạo phân trang
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();

        // 4. Gọi repository
        Page<TripFilterProjection> filteredPage = tripRepository.filterTrips(
                now, start, end, route,
                checkTimeSlots,
                slot1Start, slot1End,
                slot2Start, slot2End,
                slot3Start, slot3End,
                slot4Start, slot4End,
                checkLayouts,
                layoutKeyword1, layoutKeyword2,
                minPrice, maxPrice,
                pageable);

        // 5. Đóng gói response
        return new PagedResponse<>(
                filteredPage.getContent(),
                filteredPage.getNumber(),
                filteredPage.getSize(),
                filteredPage.getTotalElements(),
                filteredPage.getTotalPages(),
                filteredPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TripSummaryProjection> getAllTripSummaries(LocalDate date, int page, int size) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        String departureDateStr = targetDate.toString();
        Pageable pageable = PageRequest.of(page, size);
        Page<TripSummaryProjection> summaryPage = tripRepository.viewAllTripSummaries(departureDateStr, pageable);

        return new PagedResponse<>(
                summaryPage.getContent(),
                summaryPage.getNumber(),
                summaryPage.getSize(),
                summaryPage.getTotalElements(),
                summaryPage.getTotalPages(),
                summaryPage.isLast());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void executeAutoGenerateSchedule() {
        try {
            int countDate = tripRepository.countDistinctDaysWithSchedule(null, null);
            LocalDate targetStartDate = LocalDate.now();
            if (countDate > 0 && countDate <= 3) {
                targetStartDate.plusDays(4);
                tripRepository.autoGenerateWeeklySchedule(targetStartDate.toString());
            }
            System.out.println("World Machine: Đã tự động sinh gối đầu lịch tuần mới cho ngày " + targetStartDate);
        } catch (Exception e) {
            System.out.println("World Machine ERROR: Lỗi cỗ máy sinh lịch tự động: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String updateTrip(Integer tripId, TripUpdateRequest updateRequest) {
        String prompt = "";
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chuyến xe có ID: " + tripId));
        if ("COMPLETED".equals(trip.getStatus()) || "CANCELED".equals(trip.getStatus())) {
            LOGGER.error("Validation Failed: Chuyến xe ID {} đã đóng trạng thái, cấm chỉnh sửa.", tripId);
            prompt = "Chuyến xe đã hoàn thành hoặc bị hủy, không thể chỉnh sửa thông tin!";
        } else {
            try {
                trip.setDriverId(updateRequest.driverId());
                trip.setCoachId(updateRequest.coachId());
                trip.setAttendantId(updateRequest.attendantId());
                trip.setDepartureTime(updateRequest.departureTime());
                trip.setUpdatedAt(java.time.LocalDateTime.now());

                LOGGER.info("Cập nhật chuyến xe thành công. ID: {}", tripId);
                prompt = "Cập nhật thông tin chuyến xe thành công";
            } catch (Exception e) {
                LOGGER.error("ERROR: Lỗi hệ thống khi cập nhật chuyến xe {}. Lý do: {}", tripId, e.getMessage());
                prompt = "Lỗi hệ thống, cập nhật chuyến thất bại!";
            }
        }

        return prompt;
    }

    @Override
    @Transactional
    public String deleteTrip(Integer tripId) {
        String prompt = "";
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chuyến xe có ID: " + tripId));
        prompt = switch (trip.getStatus()) {
            case "COMPLETED" -> {
                LOGGER.error("Validation Failed: Chuyến xe ID {} đã hoàn thành, cấm xóa lịch sử.", tripId);
                yield "Chuyến xe đã hoàn thành hành trình, không thể xóa bỏ!";
            }
            case "CANCELED" -> {
                LOGGER.warn("Validation Failed: Chuyến xe ID {} đã bị hủy từ trước.", tripId);
                yield "Chuyến xe này đã bị hủy bỏ từ trước đó rồi.";
            }
            default -> {
                try {
                    trip.setStatus("CANCELED");
                    trip.setUpdatedAt(java.time.LocalDateTime.now());
                    LOGGER.info("Xóa mềm (Hủy) chuyến xe thành công. ID: {}", tripId);
                    yield "Xóa chuyến xe thành công!";
                } catch (Exception e) {
                    LOGGER.error("ERROR: Lỗi hệ thống không thể xóa chuyến xe {}. Lý do: {}", tripId, e.getMessage());
                    yield "Lỗi hệ thống, xóa chuyến thất bại!";
                }
            }
        };
        return prompt;
    }

    @Override
    public List<RouteDropdownDTO> findRoutesForDropdown() {
        return routeRepository.findRoutesForDropdown();
    }
    //TODO: sửa lại logic thằng của nợ này. 
    @Override
    public List<StaffProjection> getStaffNameDropDown(LocalDate date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStaffNameDropDown'");
    }

    @Override
    public List<CoachLicensePlateProjection> getCoachInfoDropDown(LocalDate date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCoachInfoDropDown'");
    }



}
