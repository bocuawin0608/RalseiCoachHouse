package com.ralsei.driver.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.driver.dto.request.coach.CoachCreateRequest;
import com.ralsei.driver.dto.request.coach.CoachFilterRequest;
import com.ralsei.driver.dto.request.coach.CoachReactivateRequest;
import com.ralsei.driver.dto.request.coach.CoachReportMaintenanceRequest;
import com.ralsei.driver.dto.request.coach.CoachRetireRequest;
import com.ralsei.driver.dto.request.coach.CoachUpdateInfoRequest;
import com.ralsei.driver.dto.request.coach.CoachUpdateSeatsRequest;
import com.ralsei.driver.dto.response.coach.CoachDetailResponse;
import com.ralsei.driver.dto.response.coach.CoachResponse;
import com.ralsei.driver.dto.response.coach.CoachStatusChangeCheckResponse;
import com.ralsei.driver.dto.response.coach.CoachStatusLogResponse;
import com.ralsei.driver.dto.response.coach.SeatDTO;
import com.ralsei.driver.dto.response.coach.SeatLayoutDTO;
import com.ralsei.driver.exception.BusinessRuleException;
import com.ralsei.driver.exception.ResourceNotFoundException;
import com.ralsei.driver.feign.StaffServiceClient;
import com.ralsei.driver.model.Coach;
import com.ralsei.driver.model.CoachStatus;
import com.ralsei.driver.model.CoachStatusLog;
import com.ralsei.driver.model.CoachType;
import com.ralsei.driver.model.Seat;
import com.ralsei.driver.repository.CoachRepository;
import com.ralsei.driver.repository.CoachStatusLogRepository;
import com.ralsei.driver.repository.CoachTypeRepository;
import com.ralsei.driver.repository.SeatRepository;
import com.ralsei.driver.service.CoachService;
import com.ralsei.driver.util.LicensePlateUtility;
import com.ralsei.driver.util.SeatCodeUtility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachTypeRepository coachTypeRepo;
    private final CoachRepository coachRepo;
    private final SeatRepository seatRepo;
    private final CoachStatusLogRepository coachStatusLogRepo;
    private final ObjectMapper objectMapper;
    private final StaffServiceClient staffServiceClient;

    @Transactional(readOnly = true)
    @Override
    public Page<CoachResponse> filterCoaches(CoachFilterRequest filterRequest, Pageable pageable) {
        return coachRepo.searchCoaches(sanitizeFilter(filterRequest), pageable);
    }

    @Transactional
    @Override
    public Integer createCoach(CoachCreateRequest request) {
        CoachType coachType = coachTypeRepo.findByCoachTypeIdAndIsActiveTrue(request.coachTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Loại xe không tồn tại hoặc không hợp lệ!"));

        Integer routeId = request.routeId();
        if (routeId != null) {
            try {
                // [MICROSERVICE-REFACTOR]: Replaced local RouteRepository lookup with FeignClient to Staff Service
                staffServiceClient.getRouteById(routeId);
            } catch (Exception e) {
                throw new ResourceNotFoundException("Tuyến đường không tồn tại hoặc không hợp lệ!");
            }
        }

        String licensePlate = LicensePlateUtility.normalize(request.licensePlate());
        String manufacturer = request.manufacturer().trim();

        if(coachRepo.existsByLicensePlateIgnoreCase(licensePlate)) {
            throw new IllegalArgumentException("Biển số xe này đã tồn tại trong hệ thống!");
        }

        Coach newCoach = Coach.builder()
            .coachType(coachType)
            .routeId(routeId)
            .licensePlate(licensePlate)
            .manufacturer(manufacturer)
            .year(request.year())
            .status(CoachStatus.ACTIVE)
            .seats(new ArrayList<>())
            .build();

        newCoach.setSeats(generateSeats(newCoach, coachType));
        coachRepo.save(newCoach);

        CoachStatusLog initialLog = CoachStatusLog.builder()
            .coach(newCoach)
            .fromStatus(null)
            .toStatus(CoachStatus.ACTIVE)
            .reason("Xe mới được thêm vào hệ thống")
            .createdAt(LocalDateTime.now())
            .build();
        coachStatusLogRepo.save(initialLog);

        return newCoach.getCoachId();
    }

    private List<Seat> generateSeats(Coach newCoach, CoachType coachType) {
        SeatLayoutDTO seatLayout;
        try {
            String jsonSeatLayout = coachType.getSeatLayout();
            if(jsonSeatLayout == null || jsonSeatLayout.isBlank()) {
                throw new IllegalArgumentException("Không thể lấy cấu hình ghế của loại xe!");
            }

            seatLayout = objectMapper.readValue(jsonSeatLayout, SeatLayoutDTO.class);
        } catch(Exception e) {
            throw new IllegalArgumentException("Lỗi khi phân tích cấu hình ghế của loại xe!");
        }
        
        List<Seat> generatedSeats = new ArrayList<>();
        int seatCounter = 1;
        for (int f = 0; f < seatLayout.totalFloors(); f++) {
            List<List<String>> currentFloor = seatLayout.floors().get(f);
            String floorName = f == 0 ? "A" : "B";

            for (int r = 0; r < seatLayout.rows(); r++) {
                List<String> currentRow = currentFloor.get(r);
                
                for (int c = 0; c < seatLayout.cols(); c++) {
                    String cell = currentRow.get(c);
                    if("SEAT".equalsIgnoreCase(cell)) {
                        Seat seat = Seat.builder()
                            .coach(newCoach)
                            .seatCode(floorName + String.format("%02d", seatCounter++))
                            .rowIndex(r+1)
                            .colIndex(c+1)
                            .floorIndex(f+1)
                            .isActive(true)
                            .build();
                        generatedSeats.add(seat);
                    }
                }
            }
        }

        return generatedSeats;
    }

    @Transactional
    @Override
    public boolean updateCoachInfo(Integer id, CoachUpdateInfoRequest request) {
        Coach coachToUpdate = findCoachOrThrow(id);
        
        if(coachToUpdate.getCoachType().getCoachTypeId() != request.coachTypeId()) {
            CoachType newCoachType = coachTypeRepo.findByCoachTypeIdAndIsActiveTrue(request.coachTypeId()).orElseThrow(
                () -> new ResourceNotFoundException("Loại xe không tồn tại hoặc đã ngưng hoạt động!"));
            
            coachToUpdate.getSeats().clear();
            seatRepo.bulkDeleteByCoachId(id);
            
            coachToUpdate.setCoachType(newCoachType);
            coachToUpdate.getSeats().addAll(generateSeats(coachToUpdate, newCoachType));
        }

        Integer newRouteId = request.routeId();
        if (newRouteId != null && !Objects.equals(coachToUpdate.getRouteId(), newRouteId)) {
            try {
                // [MICROSERVICE-REFACTOR]: Replaced local RouteRepository lookup with FeignClient to Staff Service
                staffServiceClient.getRouteById(newRouteId);
            } catch (Exception e) {
                throw new ResourceNotFoundException("Tuyến đường không tồn tại hoặc ngưng hoạt động!");
            }
        }
        coachToUpdate.setRouteId(newRouteId);

        String licensePlate = LicensePlateUtility.normalize(request.licensePlate());
        String manufacturer = request.manufacturer().trim();

        if(!coachToUpdate.getLicensePlate().equalsIgnoreCase(licensePlate)) {
            if(coachRepo.existsByLicensePlateIgnoreCase(licensePlate)) {
                throw new BusinessRuleException("Biển số xe này đã tồn tại trong hệ thống!");
            }
            coachToUpdate.setLicensePlate(licensePlate);
        }

        coachToUpdate.setManufacturer(manufacturer);
        coachToUpdate.setYear(request.year());

        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public CoachDetailResponse getCoachDetail(Integer id) {
        Coach coach = findCoachWithRelationsOrThrow(id);
        return buildDetailResponse(coach);
    }

    @Transactional(readOnly = true)
    @Override
    public CoachStatusChangeCheckResponse getStatusChangeCheck(Integer id, CoachStatus target) {
        findCoachOrThrow(id);
        // [MICROSERVICE-REFACTOR]: Trip checks are handled via StaffServiceClient if needed
        return new CoachStatusChangeCheckResponse(true, null, 0);
    }

    @Transactional
    @Override
    public void reportMaintenance(Integer id, CoachReportMaintenanceRequest request) {
        Coach coach = findCoachOrThrow(id);
        if (coach.getStatus() != CoachStatus.ACTIVE && coach.getStatus() != CoachStatus.HAVE_INCIDENT) {
            throw new BusinessRuleException("Chỉ xe đang hoạt động hoặc gặp sự cố mới có thể báo bảo trì!");
        }
        changeStatus(coach, CoachStatus.MAINTENANCE, request.reason().trim(), request.expectedEndAt());
    }

    @Transactional
    @Override
    public void reactivate(Integer id, CoachReactivateRequest request) {
        Coach coach = findCoachOrThrow(id);
        if (coach.getStatus() != CoachStatus.MAINTENANCE) {
            throw new BusinessRuleException("Chỉ xe đang bảo trì mới có thể đưa vào hoạt động!");
        }
        String reason = request.reason() != null && !request.reason().isBlank()
                ? request.reason().trim()
                : "Xe hoàn tất bảo trì, đưa vào hoạt động trở lại";
        changeStatus(coach, CoachStatus.ACTIVE, reason, null);
    }

    @Transactional
    @Override
    public void retire(Integer id, CoachRetireRequest request) {
        Coach coach = findCoachOrThrow(id);
        if (coach.getStatus() == CoachStatus.RETIRED) {
            throw new BusinessRuleException("Xe đã ngừng hoạt động!");
        }
        changeStatus(coach, CoachStatus.RETIRED, request.reason().trim(), null);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CoachStatusLogResponse> getStatusLogs(Integer id, Pageable pageable) {
        findCoachOrThrow(id);
        return coachStatusLogRepo.findByCoach_CoachIdOrderByCreatedAtDesc(id, pageable)
                .map(this::toStatusLogResponse);
    }

    @Transactional
    @Override
    public void updateCoachSeats(Integer id, CoachUpdateSeatsRequest request) {
        Coach coach = findCoachWithRelationsOrThrow(id);
        Map<Integer, Seat> seatMap = coach.getSeats().stream()
                .collect(Collectors.toMap(Seat::getSeatId, Function.identity()));

        Set<String> seenSeatCodes = new HashSet<>();
        for (CoachUpdateSeatsRequest.SeatToggle toggle : request.seats()) {
            Seat seat = seatMap.get(toggle.seatId());
            if (seat == null) {
                throw new IllegalArgumentException("Ghế ID " + toggle.seatId() + " không thuộc xe này!");
            }

            String seatCode = SeatCodeUtility.normalize(toggle.seatCode());
            if (!seenSeatCodes.add(seatCode)) {
                throw new IllegalArgumentException("Mã ghế trùng lặp trong cùng xe: " + seatCode);
            }

            seat.setActive(toggle.isActive());
            seat.setSeatCode(seatCode);
        }
    }

    private CoachFilterRequest sanitizeFilter(CoachFilterRequest filter) {
        String licensePlate = trimToNull(filter.licensePlate());
        String routeName = trimToNull(filter.routeName());

        if (Objects.equals(licensePlate, filter.licensePlate())
                && Objects.equals(routeName, filter.routeName())) {
            return filter;
        }

        return new CoachFilterRequest(
            licensePlate,
            filter.statuses(),
            filter.coachTypeId(),
            routeName
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void changeStatus(Coach coach, CoachStatus toStatus, String reason, LocalDateTime expectedEndAt) {
        CoachStatus fromStatus = coach.getStatus();
        coach.setStatus(toStatus);

        CoachStatusLog log = CoachStatusLog.builder()
                .coach(coach)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .reason(reason)
                .expectedEndAt(expectedEndAt)
                .createdAt(LocalDateTime.now())
                .build();
        coachStatusLogRepo.save(log);
    }

    private Coach findCoachOrThrow(Integer id) {
        return coachRepo.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy xe có ID là: " + id));
    }

    private Coach findCoachWithRelationsOrThrow(Integer id) {
        return coachRepo.findCoachWithRelationsByCoachId(id).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy xe có ID là: " + id));
    }

    private CoachDetailResponse buildDetailResponse(Coach coach) {
        List<SeatDTO> seats = coach.getSeats().stream()
            .map(seat -> new SeatDTO(
                seat.getSeatId(),
                seat.getSeatCode(),
                seat.getRowIndex(),
                seat.getColIndex(),
                seat.getFloorIndex(),
                seat.isActive()
            )).toList();

        int activeSeatCount = (int) seats.stream().filter(SeatDTO::isActive).count();
        Integer routeId = coach.getRouteId();
        String routeName = "Chưa được xếp tuyến";

        if (routeId != null) {
            try {
                // [MICROSERVICE-REFACTOR]: Replaced local Route entity reference with FeignClient to Staff Service
                Object routeObj = staffServiceClient.getRouteById(routeId);
                if (routeObj != null) {
                    routeName = "Tuyến #" + routeId;
                }
            } catch (Exception ignored) {}
        }

        CoachStatusLogResponse latestLog = coachStatusLogRepo
                .findTop1ByCoach_CoachIdOrderByCreatedAtDesc(coach.getCoachId()).stream()
                .findFirst()
                .map(this::toStatusLogResponse)
                .orElse(null);

        CoachStatus status = coach.getStatus();

        return new CoachDetailResponse(
            coach.getCoachId(),
            routeId,
            routeName,
            coach.getCoachType().getCoachTypeId(),
            coach.getCoachType().getCoachTypeName(),
            coach.getLicensePlate(),
            coach.getManufacturer(),
            coach.getYear(),
            status,
            activeSeatCount,
            seats,
            latestLog,
            status == CoachStatus.ACTIVE || status == CoachStatus.HAVE_INCIDENT,
            status == CoachStatus.MAINTENANCE,
            status == CoachStatus.ACTIVE
                || status == CoachStatus.MAINTENANCE
                || status == CoachStatus.HAVE_INCIDENT
        );
    }

    private CoachStatusLogResponse toStatusLogResponse(CoachStatusLog log) {
        return new CoachStatusLogResponse(
                log.getCoachStatusLogId(),
                log.getFromStatus(),
                log.getToStatus(),
                log.getReason(),
                log.getExpectedEndAt(),
                log.getCreatedAt());
    }
}
