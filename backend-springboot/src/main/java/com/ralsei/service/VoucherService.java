package com.ralsei.service;

import com.ralsei.dto.request.voucher.CreateVoucherRequest;
import com.ralsei.dto.request.voucher.UpdateVoucherRequest;
import com.ralsei.dto.request.voucher.VoucherFilterRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.voucher.VoucherMetricsResponse;
import com.ralsei.dto.response.voucher.VoucherResponse;

public interface VoucherService {
    VoucherResponse createVoucher(CreateVoucherRequest request);

    VoucherResponse updateVoucher(int id, UpdateVoucherRequest request);

    VoucherResponse getVoucherById(int id);

    PagedResponse<VoucherResponse> getAllVouchers(VoucherFilterRequest filterRequest);

    void deleteVoucher(int id);

    VoucherMetricsResponse getVoucherMetrics();
}
