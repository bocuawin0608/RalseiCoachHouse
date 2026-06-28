package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.CoachStop;
import com.ralsei.dto.projection.cargoticket.CargoTicketStopOptionProjection;

public interface CoachStopRepository extends JpaRepository<CoachStop, Integer> {

  @Query(value = """
      SELECT stopPointId AS stopPointId, stopPointName AS stopPointName
      FROM coach_stop WHERE isActive = 1 ORDER BY stopPointName
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
}
