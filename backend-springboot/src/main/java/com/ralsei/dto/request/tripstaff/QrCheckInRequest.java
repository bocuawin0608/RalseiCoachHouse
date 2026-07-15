/**
 * Request body for QR-based check-in containing the scanned QR token.
 */
package com.ralsei.dto.request.tripstaff;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents the request payload for qr check in operations.
 */
public record QrCheckInRequest(
        @NotBlank(message = "Mã QR không được để trống!")
        String qrToken
) {}
