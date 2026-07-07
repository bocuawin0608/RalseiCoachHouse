/**
 * Request body for QR-based check-in containing the scanned QR token.
 */
package com.ralsei.dto.request.tripstaff;

import jakarta.validation.constraints.NotBlank;

public record QrCheckInRequest(
        @NotBlank(message = "Mã QR không được để trống!")
        String qrToken
) {}
