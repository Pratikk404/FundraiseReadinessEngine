package com.fundraise.engine.repository;

import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByOwner(User owner);
}
