/**
 * Basic information about an accompanied child travelling with a passenger.
 */
package com.ralsei.staff.dto.response.tripstaff;

/**
 * Represents the response payload for accompanied child operations.
 */
public record AccompaniedChildResponse(
        String fullname,
        Integer birthYear
) {}
