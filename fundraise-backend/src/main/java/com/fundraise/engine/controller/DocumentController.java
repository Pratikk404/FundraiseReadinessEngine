package com.fundraise.engine.controller;

import com.fundraise.engine.entity.Document;
import com.fundraise.engine.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Documents", description = "Upload and manage compliance documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload/{companyId}")
    @Operation(summary = "Upload document", description = "Upload a cap table, incorporation certificate, board resolution, or financial statement.")
    public ResponseEntity<Document> uploadDocument(
            @PathVariable UUID companyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") Document.DocumentType type) {

        Document document = documentService.uploadDocument(companyId, file, type);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "List company documents", description = "Get all uploaded documents for a specific company.")
    public ResponseEntity<List<Document>> getDocuments(@PathVariable UUID companyId) {
        return ResponseEntity.ok(documentService.getDocumentsByCompany(companyId));
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get document", description = "Get details of a specific document including parsing status.")
    public ResponseEntity<Document> getDocument(@PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.getDocumentById(documentId));
    }
}
