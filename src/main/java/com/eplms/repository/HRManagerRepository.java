package com.eplms.repository;

import com.eplms.model.HRManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HRManagerRepository extends JpaRepository<HRManager, Long> {
    Optional<HRManager> findByEmail(String email);
    boolean existsByEmail(String email);
}
