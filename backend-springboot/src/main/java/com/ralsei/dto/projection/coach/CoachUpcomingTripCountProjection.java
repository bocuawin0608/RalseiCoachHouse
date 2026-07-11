package com.ralsei.dto.projection.coach;

/**
 * Native-query row: upcoming trip count grouped by coach.
 */
public interface CoachUpcomingTripCountProjection {
    Integer getCoachId();

    Long getUpcomingCount();
}
