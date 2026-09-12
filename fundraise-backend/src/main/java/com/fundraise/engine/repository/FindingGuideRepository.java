package com.fundraise.engine.repository;

import com.fundraise.engine.entity.FindingGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FindingGuideRepository extends JpaRepository<FindingGuide, UUID> {
    Optional<FindingGuide> findByRuleId(String ruleId);
}
