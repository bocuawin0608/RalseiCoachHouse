package com.ralsei.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.service.CoachService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
@Validated
public class CoachController {
    private final CoachService coachService;

    @GetMapping(path = {"", "/"})
    public ResponseEntity<Page<CoachResponse>> filterCoaches(
        @Valid @ModelAttribute CoachFilterRequest filterRequest,
        Pageable pageable
    ) {
        return null;
        // return ResponseEntity.ok(coachService.filterCoaches(filterRequest, pageable));
    }
}
