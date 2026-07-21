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

  /**
   * Outbound cargo route from an agency stop: route origin city matches the
   * agency city and at least one later stop exists for dropoff.
   */
  @Query(value = """
          SELECT TOP 1 rs.routeId
          FROM route_stop rs
          JOIN route r ON r.routeId = rs.routeId AND r.isActive = 1
          JOIN coach_stop agencyStop ON agencyStop.stopPointId = rs.stopPointId
          JOIN route_stop originRs ON originRs.routeId = rs.routeId
               AND originRs.stopOrder = (
                   SELECT MIN(x.stopOrder) FROM route_stop x WHERE x.routeId = rs.routeId
               )
          JOIN coach_stop originStop ON originStop.stopPointId = originRs.stopPointId
          WHERE rs.stopPointId = :pickupStopId
            AND originStop.city = agencyStop.city
            AND EXISTS (
                SELECT 1 FROM route_stop later
                WHERE later.routeId = rs.routeId AND later.stopOrder > rs.stopOrder
            )
          ORDER BY rs.routeId
          """, nativeQuery = true)
  Integer findDefaultCargoRouteIdForPickup(@Param("pickupStopId") int pickupStopId);

  /**
   * Resolves the active route that carries cargo from pickup to dropoff in order.
   */
  @Query(value = """
          SELECT TOP 1 rsPickup.routeId
          FROM route_stop rsPickup
          JOIN route_stop rsDropoff ON rsDropoff.routeId = rsPickup.routeId
          JOIN route r ON r.routeId = rsPickup.routeId AND r.isActive = 1
          WHERE rsPickup.stopPointId = :pickupStopId
            AND rsDropoff.stopPointId = :dropoffStopId
            AND rsDropoff.stopOrder > rsPickup.stopOrder
          ORDER BY rsPickup.routeId
          """, nativeQuery = true)
  Integer findRouteIdForPickupAndDropoff(
          @Param("pickupStopId") int pickupStopId,
          @Param("dropoffStopId") int dropoffStopId);
}
