package com.ralsei.driver.controller.internal;

import com.ralsei.driver.dto.response.internal.CoachTypeInfoDTO;
import com.ralsei.driver.dto.response.internal.CoachTypePriceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/coach-types")
public class InternalCoachTypeController {

    @GetMapping("/{coachTypeId}")
    public ResponseEntity<CoachTypeInfoDTO> getCoachTypeInfo(@PathVariable Integer coachTypeId) {
        // Implementation stub
        return ResponseEntity.ok(new CoachTypeInfoDTO());
    }

    @GetMapping("/{coachTypeId}/price")
    public ResponseEntity<CoachTypePriceDTO> getActivePrice(@PathVariable Integer coachTypeId) {
        // Implementation stub
        return ResponseEntity.ok(new CoachTypePriceDTO());
    }
}
