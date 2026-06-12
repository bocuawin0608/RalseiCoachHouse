package com.ralsei.dto.request.voucher;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherFilterRequest {
    private String search;
    private String discountType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private int page = 0;
    private int size = 10;
}
