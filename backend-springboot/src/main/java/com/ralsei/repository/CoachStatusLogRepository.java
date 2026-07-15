package com.ralsei.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CoachStatusLog;

/**
 * Provides persistence access for coach status log data.
 */
public interface CoachStatusLogRepository extends JpaRepository<CoachStatusLog, Integer> {

    Page<CoachStatusLog> findByCoach_CoachIdOrderByCreatedAtDesc(Integer coachId, Pageable pageable);

    List<CoachStatusLog> findTop1ByCoach_CoachIdOrderByCreatedAtDesc(Integer coachId);
}
