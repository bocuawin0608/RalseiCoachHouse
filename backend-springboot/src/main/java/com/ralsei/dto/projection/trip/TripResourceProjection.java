package com.ralsei.dto.projection.trip;

/**
 * Lightweight option returned to the staff trip forms for a coach or crew member
 * that is free for the requested departure window.
 */
/**
 * Projects the trip resourc data shape for query results.
 */
public interface TripResourceProjection {
    Integer getId();

    String getDisplayName();

    String getSecondaryText();
}
