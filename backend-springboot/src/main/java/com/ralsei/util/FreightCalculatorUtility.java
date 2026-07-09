package com.ralsei.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralized calculator for cargo freight pricing.
 *
 * <p>The business rule is: {@code Price = Freight x 3.000}. Cargo type prices
 * are surcharges only and must not replace this freight-based base price.</p>
 */
public final class FreightCalculatorUtility {

    private static final BigDecimal FREIGHT_PRICE_MULTIPLIER = new BigDecimal("3000");

    private FreightCalculatorUtility() {
        throw new UnsupportedOperationException("Utility class must not be instantiated");
    }

    /**
     * Calculates the base cargo price from freight.
     *
     * @param freight measured freight value from the logistics flow
     * @return base price rounded to whole VND
     */
    public static BigDecimal calculateBasePrice(BigDecimal freight) {
        if (freight == null || freight.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Freight must be greater than or equal to 0");
        }

        return freight.multiply(FREIGHT_PRICE_MULTIPLIER).setScale(0, RoundingMode.HALF_UP);
    }
}
