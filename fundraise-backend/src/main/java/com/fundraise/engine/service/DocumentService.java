package com.fundraise.engine.service;

import com.fundraise.engine.config.AppConfig;
import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.Document;
import com.fundraise.engine.entity.EquityEvent;
import com.fundraise.engine.entity.ShareClass;
import com.fundraise.engine.repository.CompanyRepository;
import com.fundraise.engine.repository.DocumentRepository;
import com.fundraise.engine.repository.EquityEventRepository;
import com.fundraise.engine.repository.ShareClassRepository;
import com.fundraise.engine.service.parser.CapTableParser;
import com.fundraise.engine.service.parser.ParsedCapTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final EquityEventRepository equityEventRepository;
    private final ShareClassRepository shareClassRepository;
    private final CapTableParser capTableParser;
    private final AppConfig appConfig;

    /**
     * Upload a document for a company.
     * Saves the file to disk, creates a Document entity, and triggers parsing for cap tables.
     */
    @Transactional
    public Document uploadDocument(UUID companyId, MultipartFile file, Document.DocumentType type) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        // Validate file
        validateFile(file, type);

        // Save file to disk
        String filename = generateFilename(companyId, file.getOriginalFilename());
        Path uploadDir = Paths.get(appConfig.getUploadDir());
        try {
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());

            // Create document entity
            Document document = Document.builder()
                    .company(company)
                    .type(type)
                    .filename(file.getOriginalFilename())
                    .storageUrl(filePath.toString())
                    .processingStatus(Document.ProcessingStatus.PENDING)
                    .build();

            document = documentRepository.save(document);

            // Parse cap tables immediately (synchronous for MVP)
            if (type == Document.DocumentType.CAP_TABLE) {
                parseCapTable(document, filePath, file.getOriginalFilename());
            }

            log.info("Document uploaded: {} for company {}", file.getOriginalFilename(), companyId);
            return document;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    /**
     * Get all documents for a company
     */
    public List<Document> getDocumentsByCompany(UUID companyId) {
        return documentRepository.findByCompanyId(companyId);
    }

    /**
     * Get document by ID
     */
    public Document getDocumentById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }

    /**
     * Parse cap table file and extract equity events + share classes
     */
    @Transactional
    protected void parseCapTable(Document document, Path filePath, String originalFilename) {
        try {
            document.setProcessingStatus(Document.ProcessingStatus.PROCESSING);
            documentRepository.save(document);

            ParsedCapTable parsed = capTableParser.parse(filePath, originalFilename);

            // Save equity events
            List<EquityEvent> events = parsed.getEquityEvents().stream()
                    .map(e -> {
                        e.setCompany(document.getCompany());
                        e.setSourceDocument(document);
                        return e;
                    })
                    .toList();
            equityEventRepository.saveAll(events);

            // Save share classes
            List<ShareClass> classes = parsed.getShareClasses().stream()
                    .map(sc -> {
                        sc.setCompany(document.getCompany());
                        sc.setSourceDocument(document);
                        return sc;
                    })
                    .toList();
            shareClassRepository.saveAll(classes);

            document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
            documentRepository.save(document);

            log.info("Cap table parsed: {} events, {} share classes from {}",
                    events.size(), classes.size(), originalFilename);

        } catch (Exception e) {
            log.error("Failed to parse cap table {}: {}", originalFilename, e.getMessage(), e);
            document.setProcessingStatus(Document.ProcessingStatus.FAILED);
            documentRepository.save(document);
        }
    }

    private void validateFile(MultipartFile file, Document.DocumentType type) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Filename is required");
        }

        String ext = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensions = switch (type) {
            case CAP_TABLE -> List.of("csv", "xlsx", "xls");
            case INCORPORATION, BOARD_RESOLUTION, SHA -> List.of("pdf", "docx", "doc");
            case FINANCIAL_STATEMENT -> List.of("pdf", "xlsx", "xls", "csv");
        };

        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException(
                    String.format("File type '.%s' not allowed for %s. Allowed: %s",
                            ext, type, String.join(", ", allowedExtensions)));
        }

        // Max 20MB
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("File exceeds 20MB limit");
        }
    }

    private String generateFilename(UUID companyId, String originalFilename) {
        String ext = getFileExtension(originalFilename);
        return companyId + "/" + UUID.randomUUID() + "." + ext;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex + 1);
    }
}
