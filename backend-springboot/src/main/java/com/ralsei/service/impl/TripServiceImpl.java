package com.ralsei.service.impl;

import com.ralsei.dto.projection.coach.CoachLicensePlateProjection;
import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.dto.projection.trip.StaffTripInfoProjection;
import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.projection.trip.TripStopProjection;
import com.ralsei.dto.projection.trip.TripResourceProjection;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TripService.class);
    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Set<String> WRITABLE_TRIP_STATUSES = Set.of("SCHEDULED", "IN_PROGRESS", "COMPLETED");
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
    /**
     * Inserts a new trip into the database after validating the departure time
     * and resource availability.
     *
     * @param tripRequest the request containing trip details
     * @return a message indicating the result of the operation
     */
    @Override
    public String insertTrip(TripCreateRequest tripRequest) {
        String prompt = "";
        if (tripRequest.getDepartureTime() == null
                || tripRequest.getDepartureTime().isBefore(currentMinute())) {
            LOGGER.warn("Validation failed: departure time is missing or in the past.");
            prompt = "Không thể tạo chuyến xe trong quá khứ, vui lòng tạo lại.";
        } else if (!resourcesAreAvailable(tripRequest.getRouteId(), tripRequest.getCoachId(),
                tripRequest.getDriverId(), tripRequest.getAttendantId(), tripRequest.getDepartureTime(), null)) {
            prompt = "Xe hoặc nhân sự đã có lịch trùng. Vui lòng chọn lại.";
        } else {
            try {
                tripRepository.insertTrip(tripRequest.getRouteId(), tripRequest.getCoachId(),
                        tripRequest.getDepartureTime(), tripRequest.getStatus(), tripRequest.getDriverId(),
                        tripRequest.getAttendantId());

                LOGGER.info("Tạo chuyến xe mới thành công. ID: ");
                prompt = "Tạo chuyến xe mới thành công";
            } catch (Exception e) {
                LOGGER.error("Could not create trip. Reason: {}", e.getMessage());
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
    public PagedResponse<TripSummaryProjection> getAllTripSummaries(
            LocalDate date, Integer routeId, String period, int page, int size) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now(BUSINESS_TIME_ZONE);
        String departureDateStr = targetDate.toString();
        Pageable pageable = PageRequest.of(page, size);
        String normalizedPeriod = (period == null || period.isBlank()) ? null : period.toUpperCase();
        Page<TripSummaryProjection> summaryPage = tripRepository.viewAllTripSummaries(
                departureDateStr, currentMinute(), routeId, normalizedPeriod, pageable);

        return new PagedResponse<>(
                summaryPage.getContent(),
                summaryPage.getNumber(),
                summaryPage.getSize(),
                summaryPage.getTotalElements(),
                summaryPage.getTotalPages(),
                summaryPage.isLast());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Price ranges are checkbox-friendly constants from the staff UI. Unknown
     * values are ignored so one stale browser tab cannot poison the whole
     * request. Status values are normalized to uppercase because the database
     * stores trip states as uppercase strings.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StaffTripInfoProjection> getStaffTripInfos(
            LocalDate date,
            String city,
            String timeFrom,
            String timeTo,
            String coachTypeKeyword,
            List<String> priceRanges,
            List<String> statuses,
            String driverName,
            int page,
            int size) {
        LocalDate today = LocalDate.now(BUSINESS_TIME_ZONE);
        LocalDate departureDate = (date != null) ? date : today;
        if (departureDate.isBefore(today)) {
            throw new IllegalArgumentException("Không thể tra cứu chuyến xe trong quá khứ.");
        }
        LocalDateTime selectedDayStart = departureDate.atStartOfDay();
        LocalDateTime dayStart = selectedDayStart;
        LocalDateTime nextDayStart = selectedDayStart.plusDays(1);

        Integer timeFromMinute = parseOptionalTimeToMinute(timeFrom);
        Integer timeToMinute = parseOptionalTimeToMinute(timeTo);
        if (timeFromMinute != null && timeToMinute != null && timeFromMinute > timeToMinute) {
            throw new IllegalArgumentException("Giờ bắt đầu phải nhỏ hơn hoặc bằng giờ kết thúc.");
        }

        List<String> normalizedStatuses = normalizeUppercaseFilters(statuses);
        int checkStatuses = normalizedStatuses.isEmpty() ? 0 : 1;
        if (normalizedStatuses.isEmpty()) {
            normalizedStatuses = List.of("__NO_STATUS__");
        }

        List<String> normalizedPriceRanges = normalizeUppercaseFilters(priceRanges);
        int priceLow = normalizedPriceRanges.contains("LOW") ? 1 : 0;
        int priceMiddle = normalizedPriceRanges.contains("MIDDLE") ? 1 : 0;
        int priceHigh = normalizedPriceRanges.contains("HIGH") ? 1 : 0;
        int checkPrices = (priceLow == 1 || priceMiddle == 1 || priceHigh == 1) ? 1 : 0;

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<StaffTripInfoProjection> tripPage = tripRepository.findStaffTripInfos(
                dayStart,
                nextDayStart,
                blankToNull(city),
                timeFromMinute,
                timeToMinute,
                resolveCoachTypeKeyword(coachTypeKeyword),
                checkPrices,
                priceLow,
                priceMiddle,
                priceHigh,
                checkStatuses,
                normalizedStatuses,
                blankToNull(driverName),
                pageable);

        return new PagedResponse<>(
                tripPage.getContent(),
                tripPage.getNumber(),
                tripPage.getSize(),
                tripPage.getTotalElements(),
                tripPage.getTotalPages(),
                tripPage.isLast());
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
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chuyến xe có ID: " + tripId));
        if ("COMPLETED".equals(trip.getStatus()) || isCancelled(trip.getStatus())) {
            LOGGER.error("Validation Failed: Chuyến xe ID {} đã đóng trạng thái, cấm chỉnh sửa.", tripId);
            return "Chuyến xe đã hoàn thành hoặc bị hủy, không thể chỉnh sửa thông tin!";
        }
        LocalDateTime validationTime = currentMinute();
        if (updateRequest.departureTime() == null
                || updateRequest.departureTime().isBefore(validationTime)) {
            LOGGER.warn("Rejected past trip update: tripId={}, requestedDeparture={}",
                    tripId, updateRequest.departureTime());
            return "Không thể chuyển chuyến xe về thời gian trong quá khứ. Thời gian nhận được: "
                    + updateRequest.departureTime() + ", thời gian hệ thống: " + validationTime + ".";
        }
        if (!WRITABLE_TRIP_STATUSES.contains(updateRequest.status())) {
            return "Trạng thái chuyến xe không hợp lệ.";
        }
        if (!resourcesAreAvailable(updateRequest.routeId(), updateRequest.coachId(),
                updateRequest.driverId(), updateRequest.attendantId(), updateRequest.departureTime(), tripId)) {
            return "Xe hoặc nhân sự đã có lịch trùng. Vui lòng chọn lại.";
        }
        int updated = tripRepository.updateOpenTrip(tripId, updateRequest.routeId(), updateRequest.coachId(),
                updateRequest.departureTime(), updateRequest.status(), updateRequest.driverId(),
                updateRequest.attendantId());
        if (updated != 1) {
            return "Chuyến xe đã thay đổi trạng thái, vui lòng tải lại danh sách.";
        }
        LOGGER.info("Cập nhật chuyến xe thành công. ID: {}", tripId);
        return "Cập nhật thông tin chuyến xe thành công";
    }

    @Override
    @Transactional
    public String deleteTrip(Integer tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chuyến xe có ID: " + tripId));
        if (isCancelled(trip.getStatus())) {
            return "Chuyến xe này đã bị hủy bỏ từ trước đó rồi.";
        }
        if ("COMPLETED".equals(trip.getStatus())) {
            return "Chuyến xe đã hoàn thành hành trình, không thể xóa bỏ!";
        }
        int cancelled = tripRepository.cancelOpenTrip(tripId);
        if (cancelled != 1) {
            return "Chuyến xe đã thay đổi trạng thái, vui lòng tải lại danh sách.";
        }
        LOGGER.info("Hủy chuyến xe thành công. ID: {}", tripId);
        return "Xóa chuyến xe thành công!";
    }

    /** Accepts both historical cancellation spellings stored by older code. */
    private boolean isCancelled(String status) {
        return "CANCELED".equals(status) || "CANCELLED".equals(status);
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

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TripResourceProjection> getAvailableCoaches(
            Integer routeId, LocalDateTime departureTime, Integer excludeTripId) {
        validateResourceRequest(routeId, departureTime);
        return tripRepository.findAvailableCoaches(routeId, departureTime, excludeTripId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TripResourceProjection> getAvailableDrivers(LocalDateTime departureTime, Integer excludeTripId) {
        validateResourceRequest(1, departureTime);
        return tripRepository.findAvailableStaff("DRIVER", departureTime, excludeTripId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TripResourceProjection> getAvailableAttendants(LocalDateTime departureTime, Integer excludeTripId) {
        validateResourceRequest(1, departureTime);
        return tripRepository.findAvailableStaff("ATTENDANT", departureTime, excludeTripId);
    }

    /** Ensures a resource lookup has enough data and never targets a past trip. */
    private void validateResourceRequest(Integer routeId, LocalDateTime departureTime) {
        if (routeId == null || departureTime == null
                || departureTime.isBefore(currentMinute())) {
            throw new IllegalArgumentException("Tuyến đường và thời gian khởi hành tương lai là bắt buộc.");
        }
    }

    /**
     * Matches the minute precision exposed by the staff time picker. Comparing
     * hidden seconds made the current selectable minute incorrectly look past.
     */
    private LocalDateTime currentMinute() {
        return LocalDateTime.now(BUSINESS_TIME_ZONE).truncatedTo(ChronoUnit.MINUTES);
    }

    /** Converts an optional HH:mm filter into minute-of-day for SQL comparison. */
    private Integer parseOptionalTimeToMinute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        LocalTime time = LocalTime.parse(value.trim());
        return time.getHour() * 60 + time.getMinute();
    }

    /** Normalizes repeated checkbox/filter params while dropping empty values. */
    private List<String> normalizeUppercaseFilters(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase())
                .distinct()
                .toList();
    }

    /** Returns null for blank request strings so repository SQL stays simple. */
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /**
     * Converts UI category values into safe LIKE patterns for coach type names.
     * Unknown values are ignored instead of being passed directly into SQL.
     */
    private String resolveCoachTypeKeyword(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase()) {
            case "LIMOUSINE" -> "%limousine%";
            case "LUXURY" -> "%luxury%";
            case "TRUYEN_THONG" -> "%truyền thống%";
            default -> null;
        };
    }

    /** Verifies all selected resources still remain free at write time. */
    private boolean resourcesAreAvailable(Integer routeId, Integer coachId, Integer driverId,
            Integer attendantId, LocalDateTime departureTime, Integer excludeTripId) {
        if (routeId == null || coachId == null || driverId == null || attendantId == null) {
            return false;
        }
        boolean coachFree = tripRepository.findAvailableCoaches(routeId, departureTime, excludeTripId)
                .stream().anyMatch(item -> coachId.equals(item.getId()));
        boolean driverFree = tripRepository.findAvailableStaff("DRIVER", departureTime, excludeTripId)
                .stream().anyMatch(item -> driverId.equals(item.getId()));
        boolean attendantFree = tripRepository.findAvailableStaff("ATTENDANT", departureTime, excludeTripId)
                .stream().anyMatch(item -> attendantId.equals(item.getId()));
        return coachFree && driverFree && attendantFree && !driverId.equals(attendantId);
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
