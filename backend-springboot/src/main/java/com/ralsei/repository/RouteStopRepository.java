package com.ralsei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.RouteStop;

/**
 * Provides persistence access for route stop data.
 */
public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {

  @EntityGraph(attributePaths = { "route", "coachStop" })
  List<RouteStop> findByRoute_RouteIdOrderByStopOrderAsc(int routeId);

  List<RouteStop> findByCoachStop_StopPointId(int stopPointId);

  @Query("""
      SELECT rs FROM RouteStop rs
      WHERE (:routeId = 0 OR rs.route.routeId = :routeId)
        AND (:stopPointId = 0 OR rs.coachStop.stopPointId = :stopPointId)
      """)
  @EntityGraph(attributePaths = { "route", "coachStop" })
  Page<RouteStop> searchRouteStops(
      @Param("routeId") int routeId,
      @Param("stopPointId") int stopPointId,
      Pageable pageable);

  @Query(value = """
          SELECT rs FROM RouteStop rs
          JOIN FETCH rs.coachStop cs
          JOIN rs.route r
          JOIN Trip t ON t.route = r
          WHERE t.tripId = :tripId AND cs.isActive = true
          ORDER BY rs.stopOrder ASC
      """)
  public List<RouteStop> findByTripIdWithCoachStop(@Param("tripId") Integer tripId);

  @Query("SELECT rs FROM RouteStop rs JOIN FETCH rs.coachStop WHERE rs.route.routeId = :routeId AND rs.coachStop.stopPointId = :stopPointId")
  Optional<RouteStop> findByRouteIdAndStopPointId(@Param("routeId") Integer routeId,
      @Param("stopPointId") Integer stopPointId);
}
