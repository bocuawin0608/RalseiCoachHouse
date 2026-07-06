package com.ralsei.dto.projection.tripstaff;

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
