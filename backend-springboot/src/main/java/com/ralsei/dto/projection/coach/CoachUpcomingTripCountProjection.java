package com.ralsei.dto.projection.coach;

/**
 * Native-query row: upcoming trip count grouped by coach.
 */
/**
 * Projects the coach upcoming trip coun data shape for query results.
 */
public interface CoachUpcomingTripCountProjection {
    Integer getCoachId();

    Long getUpcomingCount();
}
