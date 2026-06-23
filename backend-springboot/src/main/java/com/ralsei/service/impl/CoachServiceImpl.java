package com.ralsei.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.request.coach.CoachCreateRequest;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.request.coach.CoachUpdateInfoRequest;
import com.ralsei.dto.response.coach.CoachEditFormResponse;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.dto.response.coach.CoachViewDetailResponse;
import com.ralsei.dto.response.coach.SeatDTO;
import com.ralsei.dto.response.coach.SeatLayoutDTO;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Coach;
import com.ralsei.model.CoachType;
import com.ralsei.model.Route;
import com.ralsei.model.Seat;
import com.ralsei.model.enums.CoachStatus;
import com.ralsei.repository.CoachRepository;
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
        Coach coachToUpdate = coachRepo.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy xe có ID là: " + id));
        
        if(coachToUpdate.getCoachType().getCoachTypeId() != request.coachTypeId()) {
            if(tripRepo.existsByCoach_CoachId(id)) {
                throw new BusinessRuleException("Không thể thay đổi loại xe do xe này đang có lịch trình chuyến đi phát sinh!");
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

        if(coachToUpdate.getStatus().equals(CoachStatus.ACTIVE) && 
            ((request.status().equals(CoachStatus.MAINTENANCE) || request.status().equals(CoachStatus.RETIRED)))) {
                if(tripRepo.checkIfCoachHasTodoTrips(id)) {
                    throw new BusinessRuleException("Không thể ngừng hoạt động hoặc bảo trì xe do đang có lịch trình chuyến đi phát sinh cần thực hiện/hoàn thành!");
                }
            }
        coachToUpdate.setStatus(request.status());

        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public CoachViewDetailResponse getCoachDetailForView(Integer id) {
        Coach coachToView = coachRepo.findCoachWithRelationsByCoachId(id).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy xe có ID là: " + id));
        
        List<SeatDTO> seats = coachToView.getSeats().stream()
            .map(seat -> new SeatDTO(
                seat.getSeatId(),
                seat.getSeatCode(),
                seat.getRowIndex(),
                seat.getColIndex(),
                seat.getFloorIndex(),
                seat.isActive()
            )).toList();

        Integer activeSeatCount = (int) seats.stream().filter(s -> s.isActive()).count(); 
        String routeName = coachToView.getRoute() != null ? coachToView.getRoute().getRouteName() : "Chưa được xếp tuyến";

        return new CoachViewDetailResponse(
            coachToView.getCoachId(),
            routeName,
            coachToView.getCoachType().getCoachTypeName(),
            coachToView.getLicensePlate(),
            coachToView.getManufacturer(),
            coachToView.getYear(),
            coachToView.getStatus(),
            activeSeatCount,
            seats
        );
    }

    @Transactional(readOnly = true)
    @Override
    public CoachEditFormResponse getCoachDetailForEdit(Integer id) {
        Coach coachToUpdate = coachRepo.findCoachWithRelationsByCoachId(id).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy xe có ID là: " + id));
        
        return new CoachEditFormResponse(
            coachToUpdate.getCoachId(),
            coachToUpdate.getRoute() != null ? coachToUpdate.getRoute().getRouteId() : null,
            coachToUpdate.getCoachType().getCoachTypeId(),
            coachToUpdate.getLicensePlate(),
            coachToUpdate.getManufacturer(),
            coachToUpdate.getYear(),
            coachToUpdate.getStatus()
        );
    }
}
