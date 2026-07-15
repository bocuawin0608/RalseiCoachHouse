package com.ralsei.dto.projection.coach;

/**
 * Projects the coach license plat data shape for query results.
 */
public interface CoachLicensePlateProjection {
    String getLicensePlate();
    String getCoachTypeName();
}
