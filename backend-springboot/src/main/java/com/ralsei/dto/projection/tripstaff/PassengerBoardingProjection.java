/**
 * Projection for a passenger on a trip, including
 * check-in status and optional accompanied child information.
 */
package com.ralsei.dto.projection.tripstaff;

/**
 * Projects the passenger boardin data shape for query results.
 */
public interface PassengerBoardingProjection {

    Integer getTicketDetailId();

    String getFullName();

    String getPhone();

    String getSeatCodeSnapshot();

    String getPickupStopName();

    String getDropoffStopName();

    String getStatus();

    String getChildFullname();

    Integer getChildBirthYear();
}
