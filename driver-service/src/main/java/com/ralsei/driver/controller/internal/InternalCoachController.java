package com.ralsei.driver.controller.internal;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.driver.dto.response.internal.CoachInfoDTO;
import com.ralsei.driver.dto.response.internal.SeatDTO;
import com.ralsei.driver.model.Coach;
import com.ralsei.driver.model.CoachStatus;
import com.ralsei.driver.repository.CoachRepository;

@RestController
@RequestMapping("/api/internal/coaches")
public class InternalCoachController {

    private final CoachRepository coachRepository;

    public InternalCoachController(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @GetMapping("/{coachId}")
    public ResponseEntity<CoachInfoDTO> getCoachInfo(@PathVariable Integer coachId) {
        return coachRepository.findById(coachId)
                .map(this::mapToCoachInfo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CoachInfoDTO>> getCoachesInfo(@RequestBody List<Integer> coachIds) {
        if (coachIds == null || coachIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Coach> coaches = coachRepository.findAllById(coachIds);
        List<CoachInfoDTO> dtos = coaches.stream().map(this::mapToCoachInfo).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{coachId}/seats")
    public ResponseEntity<List<SeatDTO>> getCoachSeats(@PathVariable Integer coachId) {
        return coachRepository.findById(coachId)
                .map(coach -> {
                    if (coach.getSeats() == null) {
                        return Collections.<SeatDTO>emptyList();
                    }
                    return coach.getSeats().stream()
                            .map(s -> SeatDTO.builder()
                                    .seatId(s.getSeatId())
                                    .seatCode(s.getSeatCode())
                                    .rowIndex(s.getRowIndex())
                                    .colIndex(s.getColIndex())
                                    .floorIndex(s.getFloorIndex())
                                    .isActive(s.isActive())
                                    .build())
                            .toList();
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CoachInfoDTO>> getAvailableCoaches(
            @RequestParam(required = false) Integer routeId,
            @RequestParam(required = false) String departureTime) {
        List<Coach> coaches = coachRepository.findAll();
        List<CoachInfoDTO> dtos = coaches.stream()
                .filter(c -> c.getStatus() == CoachStatus.ACTIVE)
                .filter(c -> routeId == null || (c.getRouteId() != null && c.getRouteId().equals(routeId)))
                .map(this::mapToCoachInfo)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    private CoachInfoDTO mapToCoachInfo(Coach c) {
        return CoachInfoDTO.builder()
                .coachId(c.getCoachId())
                .licensePlate(c.getLicensePlate())
                .coachTypeName(c.getCoachType() != null ? c.getCoachType().getCoachTypeName() : null)
                .totalSeat(c.getSeats() != null && !c.getSeats().isEmpty() 
                        ? c.getSeats().size() 
                        : (c.getCoachType() != null ? c.getCoachType().getTotalSeat() : 0))
                .status(c.getStatus())
                .routeId(c.getRouteId())
                .build();
    }
}
