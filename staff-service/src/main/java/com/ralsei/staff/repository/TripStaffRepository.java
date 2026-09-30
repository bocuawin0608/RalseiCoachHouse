package com.ralsei.staff.repository;

import com.ralsei.staff.model.TripStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripStaffRepository extends JpaRepository<TripStaff, Integer> {
    
    // [MICROSERVICE-REFACTOR]: Removed coach, coach_type, and passenger_ticket JOINs.
    // Fetches base assignments. FeignClient used for extra details.
    @Query("SELECT ts FROM TripStaff ts WHERE ts.staff.staffId = :staffId AND CAST(ts.trip.departureTime AS date) = :date")
    List<com.ralsei.staff.dto.projection.tripstaff.AssignedTripProjection> findAssignedTripsByStaffAndDate(@Param("staffId") int staffId, @Param("date") LocalDate date);
    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END FROM TripStaff ts WHERE ts.trip.tripId = :tripId AND ts.staff.staffId = :staffId")
    boolean isStaffAssignedToTrip(@org.springframework.data.repository.query.Param("staffId") int staffId, @org.springframework.data.repository.query.Param("tripId") int tripId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT p FROM PassengerTicket p WHERE p.tripId = :tripId", nativeQuery = false)
    java.util.List<com.ralsei.staff.dto.projection.tripstaff.PassengerBoardingProjection> findPassengersForTrip(@org.springframework.data.repository.query.Param("tripId") Integer tripId);
}
