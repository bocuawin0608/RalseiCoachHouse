package com.ralsei.customer.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.customer.model.Route;

public interface RouteRepository extends JpaRepository<Route, Integer> {

  @Query("""
      SELECT r FROM Route r
      WHERE (:isActive IS NULL OR r.isActive = :isActive)
        AND (:search IS NULL OR r.routeName LIKE %:search%)
      """)
  Page<Route> searchRoutes(
      @Param("search") String search,
      @Param("isActive") Boolean isActive,
      Pageable pageable);

  Optional<Route> findByRouteIdAndIsActiveTrue(Integer routeId);

  @Modifying
  @Query("UPDATE Route r SET r.totalKilometers = :km, r.totalMinutes = :mins WHERE r.routeId = :routeId")
  void updateRouteTotals(@Param("routeId") int routeId, @Param("km") BigDecimal km, @Param("mins") int mins);
}
