package com.ralsei.util;

import java.math.BigDecimal;

import com.ralsei.exception.BusinessRuleException;

/** Central business policy for cargo-order and coach volume boundaries. */
public final class CargoVolumePolicy {
    private static final BigDecimal MAX_VOLUME_M3 = new BigDecimal("2.50");

    private CargoVolumePolicy() {
        // Utility class; instances would carry no state.
    }

    /**
     * Returns the maximum accepted occupied volume in cubic metres.
     *
     * @return immutable 2.50 m³ boundary
     */
    public static BigDecimal maxVolumeM3() {
        return MAX_VOLUME_M3;
    }

    /**
     * Calculates occupied volume for repeated packages of identical dimensions.
     *
     * @param dimensionVolume volume of one package in cubic metres
     * @param quantity number of identical packages
     * @return occupied volume of the detail row
     */
    public static BigDecimal occupiedVolume(BigDecimal dimensionVolume, int quantity) {
        return dimensionVolume.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Rejects an order whose summed {@code dimensionVol * quantity} is too large.
     *
     * @param occupiedVolume summed occupied volume for all order details
     * @throws BusinessRuleException when the volume is missing or exceeds 2.5 m³
     */
    public static void validateOrderVolume(BigDecimal occupiedVolume) {
        if (occupiedVolume == null || occupiedVolume.compareTo(MAX_VOLUME_M3) > 0) {
            throw new BusinessRuleException(
                    "Tổng thể tích hàng hóa (thể tích × số lượng) không được vượt quá 2,5 m³.");
        }
    }
}
