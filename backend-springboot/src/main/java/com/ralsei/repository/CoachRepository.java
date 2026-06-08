package com.ralsei.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ralsei.model.Coach;
@Repository
public interface CoachRepository extends JpaRepository<Coach, Integer> {}
