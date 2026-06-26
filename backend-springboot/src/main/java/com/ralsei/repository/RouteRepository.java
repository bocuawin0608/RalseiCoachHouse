package com.ralsei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.route.RouteNameDropdownProjection;
import com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO;
import com.ralsei.model.Route;

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

        @Query("SELECT new com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO(r.routeId, r.routeName) FROM Route r WHERE r.isActive = true ORDER BY r.routeName")
        List<RouteDropdownDTO> findRoutesForDropdown();

        @Query(value = """
                        SELECT routeName FROM ROUTE
                            """, nativeQuery = true)
        List<RouteNameDropdownProjection> routeNameDropdown();
}
