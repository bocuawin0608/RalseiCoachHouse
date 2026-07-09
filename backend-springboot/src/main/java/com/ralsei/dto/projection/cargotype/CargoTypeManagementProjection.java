package com.ralsei.dto.projection.cargotype;

import java.math.BigDecimal;

/**
 * Projection used by the staff cargo type management screen.
 *
 * <p>The screen needs cargo type information and its surcharge fields in one
 * table, so this projection keeps the read query strict and avoids stitching
 * unrelated payloads in the frontend.</p>
 */
public interface CargoTypeManagementProjection {

    int getCargoTypeId();

    String getCargoTypeName();

    boolean getActive();

    Integer getCargoTypePriceId();

    String getUnit();

    BigDecimal getPricePerUnit();
}
