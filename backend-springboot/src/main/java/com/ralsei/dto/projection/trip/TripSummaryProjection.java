package com.ralsei.dto.projection.trip;

import java.time.LocalDate;
import java.time.LocalTime;


public interface TripSummaryProjection {

    Integer getTripId();

    String getTripStatus();
    String getManufacturer();

    String getCoachTypeName();

    String getLicensePlate();

    String getCoachStatus(); 

    LocalDate getDepartureDate(); 

    LocalTime getDepartureTime(); 
    Integer getAvailableSeats(); 

    Integer getTotalSeats(); 
}