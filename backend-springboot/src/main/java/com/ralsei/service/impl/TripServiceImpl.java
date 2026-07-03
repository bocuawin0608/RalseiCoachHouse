package com.ralsei.service.impl;

import com.ralsei.dto.projection.coach.CoachLicensePlateProjection;
import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.projection.trip.TripStopProjection;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TripService.class);
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final StaffRepository staffRepository;

    /**
     * Returns stops for the route assigned to the requested trip, avoiding the
     * ambiguity of looking up a coach by date.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TripStopProjection> getTripStops(Integer tripId) {
        if (tripId == null || tripId < 1) {
            throw new IllegalArgumentException("Trip id must be greater than zero.");
        }
        return tripRepository.findTripStopsByTripId(tripId);
    }

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
        LocalDateTime currentTime = LocalDateTime.now();
        Page<TripDetailProjection> tripPage = tripRepository.findTripDetails(
                start, end, currentTime, route, pageable);

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

        List<String> normalizedTimeSlots = normalizeCustomerTimeSlots(timeSlots);
        int checkTimeSlots = normalizedTimeSlots.isEmpty() ? 0 : 1;

        Integer slot1StartMinute = null, slot1EndMinute = null;
        Integer slot2StartMinute = null, slot2EndMinute = null;
        Integer slot3StartMinute = null, slot3EndMinute = null;
        Integer slot4StartMinute = null, slot4EndMinute = null;

        if (checkTimeSlots == 1) {
            for (int i = 0; i < Math.min(normalizedTimeSlots.size(), 4); i++) {
                String slot = normalizedTimeSlots.get(i);
                if (slot != null && slot.contains("-")) {
                    String[] parts = slot.split("-");
                    Integer startMinute = parseTimeToMinuteOfDay(parts[0].trim());
                    Integer endMinute = parseTimeToMinuteOfDay(parts[1].trim());
                    if (startMinute == null || endMinute == null) {
                        continue;
                    }

                    switch (i) {
                        case 0 -> {
                            slot1StartMinute = startMinute;
                            slot1EndMinute = endMinute;
                        }
                        case 1 -> {
                            slot2StartMinute = startMinute;
                            slot2EndMinute = endMinute;
                        }
                        case 2 -> {
                            slot3StartMinute = startMinute;
                            slot3EndMinute = endMinute;
                        }
                        case 3 -> {
                            slot4StartMinute = startMinute;
                            slot4EndMinute = endMinute;
                        }
                    }
                }
            }
        }

        List<String> normalizedLayoutKeywords = normalizeCustomerCoachTypeFilters(layouts);
        int checkLayouts = normalizedLayoutKeywords.isEmpty() ? 0 : 1;
        String layoutKeyword1 = normalizedLayoutKeywords.size() > 0 ? normalizedLayoutKeywords.get(0) : null;
        String layoutKeyword2 = normalizedLayoutKeywords.size() > 1 ? normalizedLayoutKeywords.get(1) : null;
        String layoutKeyword3 = normalizedLayoutKeywords.size() > 2 ? normalizedLayoutKeywords.get(2) : null;

        // 3. Khởi tạo phân trang
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime currentTime = LocalDateTime.now();
        // 4. Gọi repository
        Page<TripFilterProjection> filteredPage = tripRepository.filterTrips(
                start, end, currentTime, route,
                checkTimeSlots,
                slot1StartMinute, slot1EndMinute,
                slot2StartMinute, slot2EndMinute,
                slot3StartMinute, slot3EndMinute,
                slot4StartMinute, slot4EndMinute,
                checkLayouts,
                layoutKeyword1,
                layoutKeyword2,
                layoutKeyword3,
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

    /**
     * Converts customer filter slot keys from the React UI into SQL-friendly time
     * ranges while still accepting direct "HH:mm-HH:mm" values from future clients.
     *
     * @param timeSlots raw request values bound by Spring from query parameters
     * @return normalized time ranges in "HH:mm-HH:mm" format
     */
    private List<String> normalizeCustomerTimeSlots(List<String> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return List.of();
        }

        List<String> normalizedSlots = new ArrayList<>();
        for (String rawSlot : timeSlots) {
            if (rawSlot == null || rawSlot.isBlank()) {
                continue;
            }

            String[] requestedSlots = rawSlot.split(",");
            for (String requestedSlot : requestedSlots) {
                String slot = requestedSlot.trim();
                if (slot.isEmpty()) {
                    continue;
                }

                switch (slot) {
                    case "EARLY_MORNING" -> normalizedSlots.add("00:00-06:00");
                    case "MORNING" -> normalizedSlots.add("06:00-12:00");
                    case "AFTERNOON" -> normalizedSlots.add("12:00-18:00");
                    case "EVENING" -> normalizedSlots.add("18:00-23:59");
                    default -> {
                        if (slot.contains("-")) {
                            normalizedSlots.add(slot);
                        }
                    }
                }
            }
        }
        return normalizedSlots;
    }

    /**
     * Parses an "HH:mm" or "HH:mm:ss" value into minutes after midnight so SQL
     * Server can compare departure times numerically instead of comparing TIME
     * to string parameters.
     *
     * @param timeValue time value from the normalized customer filter
     * @return minute of day, or {@code null} when the input is malformed
     */
    private Integer parseTimeToMinuteOfDay(String timeValue) {
        if (timeValue == null || timeValue.isBlank()) {
            return null;
        }

        String[] parts = timeValue.split(":");
        if (parts.length < 2) {
            return null;
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Converts public-site coach filter values into SQL LIKE patterns. The
     * frontend sends simple keywords: "Limousine", "luxury", or "truyền thống".
     *
     * @param layouts raw request values from the customer filter
     * @return SQL LIKE patterns for coach type filtering
     */
    private List<String> normalizeCustomerCoachTypeFilters(List<String> layouts) {
        if (layouts == null || layouts.isEmpty()) {
            return List.of();
        }

        List<String> keywords = new ArrayList<>();
        for (String rawLayout : layouts) {
            if (rawLayout == null || rawLayout.isBlank()) {
                continue;
            }

            String[] requestedLayouts = rawLayout.split(",");
            for (String requestedLayout : requestedLayouts) {
                String layout = requestedLayout.trim();
                if (layout.isEmpty()) {
                    continue;
                }

                switch (layout) {
                    case "COACH_STANDARD", "Xe Khách Truyền Thống 38 chỗ" ->
                        addUniqueKeyword(keywords, "%truyền thống%");
                    case "COACH_LIMOUSINE", "Xe Limousine VIP 20 phòng" ->
                        addUniqueKeyword(keywords, "%Limousine%");
                    case "COACH_LUXURY", "Xe Giường Nằm Luxury 32 chỗ" ->
                        addUniqueKeyword(keywords, "%luxury%");
                    default -> addUniqueKeyword(keywords, "%" + layout + "%");
                }
            }
        }
        return keywords.stream().limit(3).toList();
    }

    /**
     * Adds a SQL LIKE pattern once so combined filters do not duplicate
     * repository parameters.
     *
     * @param keywords accumulated SQL LIKE patterns
     * @param keyword candidate SQL LIKE pattern
     */
    private void addUniqueKeyword(List<String> keywords, String keyword) {
        if (!keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }


}
