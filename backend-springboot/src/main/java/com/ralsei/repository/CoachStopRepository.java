package com.ralsei.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.CoachStopDropdownProjection;
import com.ralsei.model.CoachStop;
import com.ralsei.dto.projection.cargoticket.CargoTicketStopOptionProjection;

public interface CoachStopRepository extends JpaRepository<CoachStop, Integer> {

  @Query(value = """
      SELECT cs.stopPointId AS stopPointId, cs.stopPointName AS stopPointName,
             rs.routeId AS routeId, cs.city AS city
      FROM coach_stop cs
      LEFT JOIN route_stop rs ON cs.stopPointId = rs.stopPointId
      WHERE cs.isActive = 1 ORDER BY cs.stopPointName
      """, nativeQuery = true)
  java.util.List<CargoTicketStopOptionProjection> findCargoTicketStopOptions();

  @Query("""
      SELECT c FROM CoachStop c
      WHERE (:isActive IS NULL OR c.isActive = :isActive)
        AND (:search IS NULL OR c.stopPointName LIKE %:search% OR c.address LIKE %:search% OR c.city LIKE %:search%)
      """)
  Page<CoachStop> searchCoachStops(
      @Param("search") String search,
      @Param("isActive") Boolean isActive,
      Pageable pageable);

  boolean existsByAddressIgnoreCaseAndCityIgnoreCase(String address, String city);

  @Query(value = """
      SELECT cs.stopPointId AS stopPointId, cs.stopPointName AS stopPointName,
             cs.address AS address, cs.city AS city
      FROM coach_stop cs
      LEFT JOIN ticket_agency ta ON cs.stopPointId = ta.stopPointId
      WHERE ta.ticketAgencyId IS NULL AND cs.isActive = 1
  """, nativeQuery = true)
  List<CoachStopDropdownProjection> findAvailableStops();
}
