package com.ralsei.staff.controller.internal;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.staff.repository.CoachStopRepository;

@RestController
@RequestMapping("/api/internal/coach-stops")
public class InternalCoachStopController {

    private final CoachStopRepository coachStopRepository;

    public InternalCoachStopController(CoachStopRepository coachStopRepository) {
        this.coachStopRepository = coachStopRepository;
    }

    @GetMapping("/{stopPointId}")
    public ResponseEntity<?> getCoachStopById(@PathVariable Integer stopPointId) {
        return coachStopRepository.findById(stopPointId)
                .map(cs -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("stopPointId", cs.getStopPointId());
                    map.put("stopPointName", cs.getStopPointName());
                    map.put("address", cs.getAddress());
                    map.put("city", cs.getCity());
                    map.put("latitude", cs.getLatitude());
                    map.put("longitude", cs.getLongitude());
                    map.put("isActive", cs.isActive());
                    return ResponseEntity.ok((Object) map);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
