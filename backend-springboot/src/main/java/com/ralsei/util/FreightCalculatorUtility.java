package com.ralsei.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralizes the business formulas used to calculate cargo freight and price.
 *
 * <p>The calculation order is:</p>
 * <ol>
 *   <li>{@code Freight = (V / 1.2) x 300}</li>
 *   <li>{@code Base price = Freight x 3,000}</li>
 *   <li>{@code Final price = Base price + cargo type surcharge}</li>
 * </ol>
 *
 * <p>A cargo type price is only a surcharge. It must never replace the
 * freight-based base price.</p>
 */
public final class FreightCalculatorUtility {

    private static final BigDecimal VOLUME_DIVISOR = new BigDecimal("1.2");
    private static final BigDecimal VOLUMETRIC_FREIGHT_MULTIPLIER = new BigDecimal("300");
    private static final BigDecimal FREIGHT_PRICE_MULTIPLIER = new BigDecimal("3000");

    private FreightCalculatorUtility() {
        throw new UnsupportedOperationException("Utility class must not be instantiated");
    }

    /**
     * Calculates cargo volume from its three dimensions.
     *
     * @param length cargo length in the unit used by the booking flow
     * @param width cargo width in the same unit as {@code length}
     * @param height cargo height in the same unit as {@code length}
     * @return cargo volume ({@code length x width x height})
     * @throws IllegalArgumentException when a dimension is null or negative
     */
    public static BigDecimal calculateVolume(
            BigDecimal length,
            BigDecimal width,
            BigDecimal height) {
        validateNonNegative(length, "Length");
        validateNonNegative(width, "Width");
        validateNonNegative(height, "Height");

        return length.multiply(width).multiply(height);
    }

    /**
     * Calculates freight from cargo volume using the approved logistics rule:
     * {@code Freight = (V / 1.2) x 300}.
     *
     * @param volume cargo volume ({@code V})
     * @return freight without intermediate rounding
     * @throws IllegalArgumentException when volume is null or negative
     */
    /**
     * Executes the calculate freight operation.
     *
     * @param volume the value supplied for this operation
     *
     * @return the operation result
     */
    public static BigDecimal calculateFreight(BigDecimal volume) {
        validateNonNegative(volume, "Volume");

        // Multiplication is evaluated first to keep the equivalent business
        // formula exact and avoid rounding before the final VND calculation.
        return volume
                .multiply(VOLUMETRIC_FREIGHT_MULTIPLIER)
                .divide(VOLUME_DIVISOR);
    }

    /**
     * Calculates the base cargo price using {@code Price = Freight x 3,000}.
     *
     * @param freight freight calculated by {@link #calculateFreight(BigDecimal)}
     * @return base price rounded to whole VND
     * @throws IllegalArgumentException when freight is null or negative
     */
    /**
     * Executes the calculate base price operation.
     *
     * @param freight the value supplied for this operation
     *
     * @return the operation result
     */
    public static BigDecimal calculateBasePrice(BigDecimal freight) {
        validateNonNegative(freight, "Freight");

        return freight.multiply(FREIGHT_PRICE_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the base cargo price directly from cargo volume.
     *
     * @param volume cargo volume ({@code V})
     * @return freight-based base price rounded to whole VND
     */
    /**
     * Executes the calculate base price from volume operation.
     *
     * @param volume the value supplied for this operation
     *
     * @return the operation result
     */
    public static BigDecimal calculateBasePriceFromVolume(BigDecimal volume) {
        return calculateBasePrice(calculateFreight(volume));
    }

    /**
     * Calculates the final cargo price from volume and cargo type surcharge.
     *
     * @param volume cargo volume ({@code V})
     * @param surcharge cargo type surcharge in VND
     * @return final cargo price rounded to whole VND
     * @throws IllegalArgumentException when volume or surcharge is null or negative
     */
    public static BigDecimal calculatePriceWithSurcharge(
            BigDecimal volume,
            BigDecimal surcharge) {
        validateNonNegative(surcharge, "Surcharge");

        return calculateBasePriceFromVolume(volume)
                .add(surcharge)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Rejects invalid values before they can produce an incorrect cargo charge.
     *
     * @param value value to validate
     * @param fieldName business field name used in the validation message
     */
    private static void validateNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
    }
}
