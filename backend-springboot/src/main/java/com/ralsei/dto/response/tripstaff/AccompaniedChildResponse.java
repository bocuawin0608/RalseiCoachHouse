/**
 * Basic information about an accompanied child travelling with a passenger.
 */
package com.ralsei.dto.response.tripstaff;

public record AccompaniedChildResponse(
        String fullname,
        Integer birthYear
) {}
