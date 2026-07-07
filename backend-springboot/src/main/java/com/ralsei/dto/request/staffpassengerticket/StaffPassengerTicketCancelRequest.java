package com.ralsei.dto.request.staffpassengerticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StaffPassengerTicketCancelRequest(
    @NotBlank(message = "Vui lòng nhập tên ngân hàng.")
    @Size(max = 100, message = "Tên ngân hàng không được vượt quá 100 ký tự.")
    String bankName,

    @NotBlank(message = "Vui lòng nhập tên chủ tài khoản.")
    @Size(max = 150, message = "Tên chủ tài khoản không được vượt quá 150 ký tự.")
    String accountHolder,

    @NotBlank(message = "Vui lòng nhập số tài khoản.")
    @Pattern(regexp = "[0-9]{6,30}", message = "Số tài khoản phải gồm từ 6 đến 30 chữ số.")
    String accountNumber,

    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự.")
    String reason
) {}
