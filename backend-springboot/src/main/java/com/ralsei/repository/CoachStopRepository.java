package com.ralsei.repository;

import com.ralsei.model.CoachStop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoachStopRepository extends JpaRepository<CoachStop, Integer> {

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
