package com.ralsei.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ralsei.model.RouteStop;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {

  @EntityGraph(attributePaths = { "route", "coachStop" })
  List<RouteStop> findByRoute_RouteIdOrderByStopOrderAsc(int routeId);

  @EntityGraph(attributePaths = { "route", "coachStop" })
  List<RouteStop> findByRoute_RouteIdAndIsActiveOrderByStopOrderAsc(int routeId, boolean isActive);

  List<RouteStop> findByCoachStop_StopPointId(int stopPointId);

  @Query("""
      SELECT rs FROM RouteStop rs
      WHERE (:isActive IS NULL OR rs.isActive = :isActive)
        AND (:routeId IS NULL OR rs.route.routeId = :routeId)
        AND (:stopPointId IS NULL OR rs.coachStop.stopPointId = :stopPointId)
      """)
  @EntityGraph(attributePaths = { "route", "coachStop" })
  Page<RouteStop> searchRouteStops(
      @Param("routeId") int routeId,
      @Param("stopPointId") int stopPointId,
      @Param("isActive") Boolean isActive,
      Pageable pageable);
}
