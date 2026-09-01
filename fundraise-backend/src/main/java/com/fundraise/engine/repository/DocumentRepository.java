package com.fundraise.engine.repository;

import com.fundraise.engine.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByCompanyId(UUID companyId);
    List<Document> findByCompanyIdAndType(UUID companyId, Document.DocumentType type);
}
