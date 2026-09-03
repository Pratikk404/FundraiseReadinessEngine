package com.fundraise.engine.controller;

import com.fundraise.engine.entity.Document;
import com.fundraise.engine.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload a document for a company
     */
    @PostMapping("/upload/{companyId}")
    public ResponseEntity<Document> uploadDocument(
            @PathVariable UUID companyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") Document.DocumentType type) {

        Document document = documentService.uploadDocument(companyId, file, type);
        return ResponseEntity.ok(document);
    }

    /**
     * Get all documents for a company
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Document>> getDocuments(@PathVariable UUID companyId) {
        return ResponseEntity.ok(documentService.getDocumentsByCompany(companyId));
    }

    /**
     * Get a specific document
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<Document> getDocument(@PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.getDocumentById(documentId));
    }
}
