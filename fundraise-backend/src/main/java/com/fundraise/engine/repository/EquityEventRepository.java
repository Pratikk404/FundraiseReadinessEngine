package com.fundraise.engine.repository;

import com.fundraise.engine.entity.EquityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EquityEventRepository extends JpaRepository<EquityEvent, Long> {
    List<EquityEvent> findByCompanyIdOrderByEventDateAsc(UUID companyId);
}
