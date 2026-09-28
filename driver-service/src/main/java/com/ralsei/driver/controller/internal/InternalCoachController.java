package com.ralsei.driver.controller.internal;

import com.ralsei.driver.dto.response.internal.CoachInfoDTO;
import com.ralsei.driver.dto.response.internal.SeatDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/internal/coaches")
public class InternalCoachController {

    @GetMapping("/{coachId}")
    public ResponseEntity<CoachInfoDTO> getCoachInfo(@PathVariable Integer coachId) {
        // Implementation stub
        return ResponseEntity.ok(new CoachInfoDTO());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CoachInfoDTO>> getCoachesInfo(@RequestBody List<Integer> coachIds) {
        // Implementation stub
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/{coachId}/seats")
    public ResponseEntity<List<SeatDTO>> getCoachSeats(@PathVariable Integer coachId) {
        // Implementation stub
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CoachInfoDTO>> getAvailableCoaches(
            @RequestParam(required = false) Integer routeId,
            @RequestParam(required = false) String excludeDate) {
        // Implementation stub
        return ResponseEntity.ok(Collections.emptyList());
    }
}
