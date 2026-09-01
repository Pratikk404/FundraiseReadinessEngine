package com.fundraise.engine.repository;

import com.fundraise.engine.entity.ReadinessScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReadinessScoreRepository extends JpaRepository<ReadinessScore, Long> {
    List<ReadinessScore> findByCompanyIdOrderByComputedAtDesc(UUID companyId);
    List<ReadinessScore> findTopByCompanyIdOrderByComputedAtDesc(UUID companyId);
}
