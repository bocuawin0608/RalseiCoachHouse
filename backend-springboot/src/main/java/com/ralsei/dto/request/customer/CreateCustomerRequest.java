package com.ralsei.dto.request.customer;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateCustomerRequest
 */

public record CreateCustomerRequest(
    @NotBlank(message = "Tên khách hàng không được để trống.")
    @Size(max = 100, message = "Tên khách hàng không được vượt quá 100 ký tự.")
    String customerName,

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự.")
    String phone,

    @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
    String email,

    LocalDate dob
) {}
