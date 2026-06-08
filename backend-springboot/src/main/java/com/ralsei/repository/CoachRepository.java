package com.ralsei.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
>>>>>>> main

import com.ralsei.dto.projection.CoachProjection;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.model.Coach;
<<<<<<< HEAD
@Repository
public interface CoachRepository extends JpaRepository<Coach, Integer> {}
=======

public interface CoachRepository extends JpaRepository<Coach, Integer> {
    
    @Query(value = """
           SELECT  
        """, nativeQuery = true,
        countQuery = """
            SELECT     
        """)
    Page<CoachProjection> searchCoaches(
        @Param("filter") CoachFilterRequest filter,
        Pageable pageable 
    );
    //phải sắp xếp order by theo thứ tự status: active, maintenance, retired, rồi đến coach type
}
>>>>>>> main
