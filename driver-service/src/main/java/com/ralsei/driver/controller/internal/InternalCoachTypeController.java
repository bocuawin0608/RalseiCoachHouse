package com.ralsei.driver.controller.internal;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.driver.dto.response.internal.CoachTypeInfoDTO;
import com.ralsei.driver.dto.response.internal.CoachTypePriceDTO;
import com.ralsei.driver.model.CoachTypePrice;
import com.ralsei.driver.repository.CoachTypePriceRepository;
import com.ralsei.driver.repository.CoachTypeRepository;

@RestController
@RequestMapping("/api/internal/coach-types")
public class InternalCoachTypeController {

    private final CoachTypeRepository coachTypeRepository;
    private final CoachTypePriceRepository coachTypePriceRepository;

    public InternalCoachTypeController(CoachTypeRepository coachTypeRepository, CoachTypePriceRepository coachTypePriceRepository) {
        this.coachTypeRepository = coachTypeRepository;
        this.coachTypePriceRepository = coachTypePriceRepository;
    }

    @GetMapping("/{coachTypeId}")
    public ResponseEntity<CoachTypeInfoDTO> getCoachTypeInfo(@PathVariable Integer coachTypeId) {
        return coachTypeRepository.findById(coachTypeId)
                .map(ct -> CoachTypeInfoDTO.builder()
                        .coachTypeId(ct.getCoachTypeId())
                        .coachTypeName(ct.getCoachTypeName())
                        .totalSeat(ct.getTotalSeat())
                        .seatLayout(ct.getSeatLayout())
                        .isActive(ct.isActive())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{coachTypeId}/price")
    public ResponseEntity<CoachTypePriceDTO> getActivePrice(@PathVariable Integer coachTypeId) {
        List<CoachTypePrice> prices = coachTypePriceRepository.findByCoachType_CoachTypeIdOrderByStartEffectiveDateDesc(coachTypeId);
        LocalDateTime now = LocalDateTime.now();
        CoachTypePrice active = prices.stream()
                .filter(p -> (p.getStartEffectiveDate() == null || !now.isBefore(p.getStartEffectiveDate()))
                          && (p.getEndEffectiveDate() == null || !now.isAfter(p.getEndEffectiveDate())))
                .findFirst()
                .orElse(prices.isEmpty() ? null : prices.get(0));

        if (active == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(CoachTypePriceDTO.builder()
                .coachTypePriceId(active.getCoachTypePriceId())
                .seatPrice(active.getSeatPrice())
                .startEffectiveDate(active.getStartEffectiveDate())
                .endEffectiveDate(active.getEndEffectiveDate())
                .build());
    }
}
