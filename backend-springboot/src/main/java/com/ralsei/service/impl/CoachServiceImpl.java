package com.ralsei.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.request.coach.CoachCreateRequest;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.request.coach.CoachReactivateRequest;
import com.ralsei.dto.request.coach.CoachReportMaintenanceRequest;
import com.ralsei.dto.request.coach.CoachRetireRequest;
import com.ralsei.dto.request.coach.CoachUpdateInfoRequest;
import com.ralsei.dto.request.coach.CoachUpdateSeatsRequest;
import com.ralsei.dto.response.coach.CoachDetailResponse;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.dto.response.coach.CoachStatusChangeCheckResponse;
import com.ralsei.dto.response.coach.CoachStatusLogResponse;
import com.ralsei.dto.response.coach.SeatDTO;
import com.ralsei.dto.response.coach.SeatLayoutDTO;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Coach;
import com.ralsei.model.CoachStatusLog;
import com.ralsei.model.CoachType;
import com.ralsei.model.Route;
import com.ralsei.model.Seat;
import com.ralsei.model.enums.CoachStatus;
import com.ralsei.repository.CoachRepository;
import com.ralsei.repository.CoachStatusLogRepository;
import com.ralsei.repository.CoachTypeRepository;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.SeatRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.CoachService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachTypeRepository coachTypeRepo;
    private final CoachRepository coachRepo;
    private final RouteRepository routeRepo;
    private final TripRepository tripRepo;
    private final SeatRepository seatRepo;
    private final CoachStatusLogRepository coachStatusLogRepo;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<CoachResponse> filterCoaches(CoachFilterRequest filterRequest, Pageable pageable) {
        return coachRepo.searchCoaches(filterRequest, pageable);
    }

    @Transactional
    @Override
    public Integer createCoach(CoachCreateRequest request) {
        CoachType coachType = coachTypeRepo.findByCoachTypeIdAndIsActiveTrue(request.coachTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Loại xe không tồn tại hoặc không hợp lệ!"));

        Route route = request.routeId() == null ? null : routeRepo.findByRouteIdAndIsActiveTrue(request.routeId())
            .orElseThrow(() -> new ResourceNotFoundException("Tuyến đường không tồn tại hoặc không hợp lệ!"));

        if(coachRepo.existsByLicensePlateIgnoreCase(request.licensePlate())) {
            throw new IllegalArgumentException("Biển số xe này đã tồn tại trong hệ thống!");
        }

        Coach newCoach = Coach.builder()
            .coachType(coachType)
            .route(route)
            .licensePlate(request.licensePlate())
            .manufacturer(request.manufacturer())
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
                throw new IllegalArgumentException("Không thể lấy cấu hình ghế ghế của loại xe!");
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
                    String cell =  currentRow.get(c);
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
            if(tripRepo.existsByCoach_CoachId(id)) {
                throw new BusinessRuleException("Không thể thay đổi loại xe do xe này đã có lịch trình chuyến đi phát sinh!");
            }

            CoachType newCoachType = coachTypeRepo.findByCoachTypeIdAndIsActiveTrue(request.coachTypeId()).orElseThrow(
                () -> new ResourceNotFoundException("Loại xe không tồn tại hoặc đã ngưng hoạt động!"));
            
            coachToUpdate.getSeats().clear();
            seatRepo.bulkDeleteByCoachId(id);
            
            coachToUpdate.setCoachType(newCoachType);
            coachToUpdate.getSeats().addAll(generateSeats(coachToUpdate, newCoachType));
        }
        //vì thằng bulkDelete kia có @Modifying nên nó sẽ đc chạy trước, rồi sau đó con hibernate mới soi coachToUpdate có gì thay đổi thì nó tổng hợp để chạy lệnh save() ngầm dưới DB

        Integer oldRouteId = coachToUpdate.getRoute() != null ? coachToUpdate.getRoute().getRouteId() : null;
        if(!Objects.equals(oldRouteId, request.routeId())) {
            Route newRoute = request.routeId() != null ? routeRepo.findByRouteIdAndIsActiveTrue(request.routeId()).orElseThrow(
                () -> new ResourceNotFoundException("Tuyến đường không tồn tại hoặc ngưng hoạt động!")) : null;
            coachToUpdate.setRoute(newRoute);
        }

        if(!coachToUpdate.getLicensePlate().equalsIgnoreCase(request.licensePlate())) {
            if(coachRepo.existsByLicensePlateIgnoreCase(request.licensePlate())) {
                throw new BusinessRuleException("Biển số xe này đã tồn tại trong hệ thống!");
            }
            coachToUpdate.setLicensePlate(request.licensePlate());
        }

        coachToUpdate.setManufacturer(request.manufacturer());
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
        if (target == CoachStatus.MAINTENANCE || target == CoachStatus.RETIRED) {
            long upcoming = tripRepo.countUpcomingTripsByCoachId(id);
            if (upcoming > 0) {
                return new CoachStatusChangeCheckResponse(
                    false,
                    "Không thể đổi trạng thái do xe đang có lịch trình chuyến đi cần thực hiện/hoàn thành!",
                    upcoming);
            }
        }
        return new CoachStatusChangeCheckResponse(true, null, 0);
    }

    @Transactional
    @Override
    public void reportMaintenance(Integer id, CoachReportMaintenanceRequest request) {
        Coach coach = findCoachOrThrow(id);
        if (coach.getStatus() != CoachStatus.ACTIVE) {
            throw new BusinessRuleException("Chỉ xe đang hoạt động mới có thể báo bảo trì!");
        }
        assertNoUpcomingTrips(id);
        changeStatus(coach, CoachStatus.MAINTENANCE, request.reason(), request.expectedEndAt());
    }

    @Transactional
    @Override
    public void reactivate(Integer id, CoachReactivateRequest request) {
        Coach coach = findCoachOrThrow(id);
        if (coach.getStatus() != CoachStatus.MAINTENANCE) {
            throw new BusinessRuleException("Chỉ xe đang bảo trì mới có thể đưa vào hoạt động!");
        }
        String reason = request.reason() != null && !request.reason().isBlank()
                ? request.reason()
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
        assertNoUpcomingTrips(id);
        changeStatus(coach, CoachStatus.RETIRED, request.reason(), null);
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

        for (CoachUpdateSeatsRequest.SeatToggle toggle : request.seats()) {
            Seat seat = seatMap.get(toggle.seatId());
            if (seat == null) {
                throw new IllegalArgumentException("Ghế ID " + toggle.seatId() + " không thuộc xe này!");
            }
            seat.setActive(toggle.isActive());
        }
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

    private void assertNoUpcomingTrips(Integer coachId) {
        if (tripRepo.checkIfCoachHasTodoTrips(coachId)) {
            throw new BusinessRuleException(
                    "Không thể ngừng hoạt động hoặc bảo trì xe do đang có lịch trình chuyến đi phát sinh cần thực hiện/hoàn thành!");
        }
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
        String routeName = coach.getRoute() != null ? coach.getRoute().getRouteName() : "Chưa được xếp tuyến";
        Integer routeId = coach.getRoute() != null ? coach.getRoute().getRouteId() : null;

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
            status == CoachStatus.ACTIVE,
            status == CoachStatus.MAINTENANCE,
            status == CoachStatus.ACTIVE || status == CoachStatus.MAINTENANCE
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
