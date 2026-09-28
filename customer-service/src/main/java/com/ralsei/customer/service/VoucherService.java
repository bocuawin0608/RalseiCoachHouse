package com.ralsei.customer.service;

import java.math.BigDecimal;
import java.util.List;

import com.ralsei.customer.dto.request.voucher.CreateVoucherRequest;
import com.ralsei.customer.dto.request.voucher.UpdateVoucherRequest;
import com.ralsei.customer.dto.request.voucher.VoucherFilterRequest;
import com.ralsei.customer.dto.response.PagedResponse;
import com.ralsei.customer.dto.response.passengerbooking.VoucherDTO;
import com.ralsei.customer.dto.response.voucher.VoucherMetricsResponse;
import com.ralsei.customer.dto.response.voucher.VoucherResponse;
import com.ralsei.customer.model.Voucher;

/**
 * Provides the business service contract for voucher.
 */
public interface VoucherService {
    VoucherResponse createVoucher(CreateVoucherRequest request);

    VoucherResponse updateVoucher(int id, UpdateVoucherRequest request);

    VoucherResponse getVoucherById(int id);

    PagedResponse<VoucherResponse> getAllVouchers(VoucherFilterRequest filterRequest);

    void deleteVoucher(int id);

    VoucherMetricsResponse getVoucherMetrics();

    List<VoucherDTO> getEligibleVouchers();
    Voucher getEligibleVoucher(Integer voucherId, BigDecimal currentOrderValue);
    int incrementUsedCountIfAvailable(Integer voucherId);
    int decrementUsedCountIfApplied(Integer voucherId);
}
