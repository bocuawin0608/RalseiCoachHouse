package com.ralsei.controller;

import com.ralsei.dto.request.voucher.CreateVoucherRequest;
import com.ralsei.dto.request.voucher.UpdateVoucherRequest;
import com.ralsei.dto.request.voucher.VoucherFilterRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.voucher.VoucherMetricsResponse;
import com.ralsei.dto.response.voucher.VoucherResponse;
import com.ralsei.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.createVoucher(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Integer id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<VoucherResponse>> getAllVouchers(@Valid @ModelAttribute VoucherFilterRequest filterRequest) {
        return ResponseEntity.ok(voucherService.getAllVouchers(filterRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherResponse> updateVoucher(@PathVariable Integer id, @Valid @RequestBody UpdateVoucherRequest request) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics")
    public ResponseEntity<VoucherMetricsResponse> getVoucherMetrics() {
        return ResponseEntity.ok(voucherService.getVoucherMetrics());
    }
}
