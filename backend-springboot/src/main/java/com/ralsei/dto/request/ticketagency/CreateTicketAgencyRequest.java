package com.ralsei.dto.request.ticketagency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * CreateTicketAgencyRequest
 */

/**
 * Represents the request payload for create ticket agency operations.
 */
public record CreateTicketAgencyRequest(
    @NotBlank(message = "Tên bến xe không được để trống.")
    @Size(max = 200, message = "Tên bến xe không được vượt quá 200 ký tự.")
    String ticketAgencyName,

    @NotNull(message = "Vui lòng chọn điểm dừng.")
    @Positive(message = "ID điểm dừng phải là số dương.")
    Integer stopPointId
) {}
