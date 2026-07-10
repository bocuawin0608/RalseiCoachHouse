package com.ralsei.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/**
 * Verifies the cargo freight and pricing business formulas independently from
 * persistence and transport concerns.
 */
class FreightCalculatorUtilityTest {

    /** Verifies {@code Freight = (V / 1.2) x 300}. */
    @Test
    void calculateFreightUsesApprovedVolumeFormula() {
        BigDecimal freight = FreightCalculatorUtility.calculateFreight(new BigDecimal("1.2"));

        assertEquals(0, freight.compareTo(new BigDecimal("300")));
    }

    /** Verifies {@code Price = Freight x 3,000}. */
    @Test
    void calculateBasePriceFromVolumeUsesFreightPriceFormula() {
        BigDecimal price = FreightCalculatorUtility.calculateBasePriceFromVolume(new BigDecimal("1.2"));

        assertEquals(0, price.compareTo(new BigDecimal("900000")));
    }

    /** Verifies cargo type pricing is added only as a surcharge. */
    @Test
    void calculatePriceWithSurchargeAddsSurchargeAfterBasePrice() {
        BigDecimal price = FreightCalculatorUtility.calculatePriceWithSurcharge(
                new BigDecimal("1.2"),
                new BigDecimal("60000"));

        assertEquals(0, price.compareTo(new BigDecimal("960000")));
    }

    /** Verifies invalid negative logistics values are rejected immediately. */
    @Test
    void calculateFreightRejectsNegativeVolume() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FreightCalculatorUtility.calculateFreight(new BigDecimal("-1")));
    }
}
