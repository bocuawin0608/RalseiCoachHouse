package com.ralsei.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ralsei.exception.BusinessRuleException;

/** Verifies the exact accepted and rejected edges of the cargo volume policy. */
class CargoVolumePolicyTest {

    /** Confirms an order exactly at 2.5 m³ remains valid. */
    @Test
    void acceptsOrderAtMaximumVolume() {
        assertDoesNotThrow(() -> CargoVolumePolicy.validateOrderVolume(new BigDecimal("2.50")));
    }

    /** Confirms even a small amount above 2.5 m³ is rejected. */
    @Test
    void rejectsOrderAboveMaximumVolume() {
        assertThrows(BusinessRuleException.class,
                () -> CargoVolumePolicy.validateOrderVolume(new BigDecimal("2.500001")));
    }

    /** Reproduces the reported large-quantity bypass with 99 identical items. */
    @Test
    void rejectsNinetyNineItemsWhoseCombinedVolumeExceedsMaximum() {
        BigDecimal occupiedVolume = CargoVolumePolicy.occupiedVolume(new BigDecimal("0.03"), 99);

        assertThrows(BusinessRuleException.class,
                () -> CargoVolumePolicy.validateOrderVolume(occupiedVolume));
    }
}
