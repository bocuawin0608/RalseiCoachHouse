package com.ralsei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ralsei.model.CoachTypePrice;

/**
 * Provides persistence access for coach type price data.
 */
public interface CoachTypePriceRepository extends JpaRepository<CoachTypePrice, Integer> {

    List<CoachTypePrice> findByCoachType_CoachTypeIdOrderByStartEffectiveDateDesc(Integer coachTypeId);
}
