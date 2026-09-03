package com.fundraise.engine.repository;

import com.fundraise.engine.entity.Finding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FindingRepository extends JpaRepository<Finding, Long> {
    List<Finding> findByCompanyId(UUID companyId);
    List<Finding> findByCompanyIdAndCategory(UUID companyId, String category);
    List<Finding> findByCompanyIdAndResolvedFalse(UUID companyId);
    long countByCompanyIdAndResolvedFalse(UUID companyId);
    long countByResolvedFalse();
}
