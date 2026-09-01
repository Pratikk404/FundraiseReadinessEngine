package com.fundraise.engine.repository;

import com.fundraise.engine.entity.EsopGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EsopGrantRepository extends JpaRepository<EsopGrant, UUID> {
    List<EsopGrant> findByCompanyId(UUID companyId);
}
