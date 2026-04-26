package com.eplms.repository;

import com.eplms.model.PerformanceGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceGoalRepository extends JpaRepository<PerformanceGoal, Long> {
    List<PerformanceGoal> findByEmployeeId(Long employeeId);
}
