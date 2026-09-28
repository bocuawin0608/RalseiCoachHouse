package com.ralsei.auth.dto.projection;

/**
 * Projects the coach stop dropdow data shape for query results.
 */
public interface CoachStopDropdownProjection {
    Integer getStopPointId();
    String getStopPointName();
    String getAddress();
    String getCity();
}