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
    @Query("SELECT ts FROM TripStaff ts WHERE ts.staff.id = :staffId AND CAST(ts.trip.departureTime AS date) = :date")
    List<TripStaff> findAssignedTripsByStaffAndDate(@Param("staffId") int staffId, @Param("date") LocalDate date);
}
