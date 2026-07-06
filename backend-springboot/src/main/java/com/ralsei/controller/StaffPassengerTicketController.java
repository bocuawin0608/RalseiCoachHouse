package com.ralsei.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketListItemResponse;
import com.ralsei.service.passengerticket.StaffPassengerTicketQueryService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/staff/passenger-tickets")
@PreAuthorize("hasRole('TICKET_STAFF')")
@RequiredArgsConstructor
@Validated
public class StaffPassengerTicketController {

    private final StaffPassengerTicketQueryService queryService;

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<StaffPassengerTicketListItemResponse>> search(
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String ticketCode,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer routeId,
        @RequestParam(required = false) Integer tripId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(queryService.search(
            phone, ticketCode, status, routeId, tripId, departureDate, page, size
        ));
    }

    @GetMapping("/{ticketCode:[A-Za-z0-9_-]+}")
    public ResponseEntity<StaffPassengerTicketDetailResponse> getDetail(
        @PathVariable @Pattern(regexp = "[A-Za-z0-9_-]{3,64}") String ticketCode
    ) {
        return ResponseEntity.ok(queryService.getDetail(ticketCode));
    }

    @GetMapping(value = "/{ticketCode:[A-Za-z0-9_-]+}/details/{detailId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getSeatQr(
        @PathVariable @Pattern(regexp = "[A-Za-z0-9_-]{3,64}") String ticketCode,
        @PathVariable("detailId") @Min(1) Integer ticketDetailId
    ) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.IMAGE_PNG)
            .body(queryService.getSeatQrImage(ticketCode, ticketDetailId));
    }
}
