package com.fundraise.engine.controller;

import com.fundraise.engine.dto.FindingDto;
import com.fundraise.engine.dto.ReadinessScoreDto;
import com.fundraise.engine.service.ComplianceService;
import com.fundraise.engine.service.GapReportService;
import com.fundraise.engine.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ComplianceController {

    private final ComplianceService complianceService;
    private final GapReportService gapReportService;
    private final PdfExportService pdfExportService;

    /**
     * Run full compliance check for a company
     */
    @PostMapping("/check/{companyId}")
    public ResponseEntity<Map<String, Object>> runComplianceCheck(@PathVariable UUID companyId) {
        return ResponseEntity.ok(complianceService.runComplianceCheck(companyId));
    }

    /**
     * Get findings for a company
     */
    @GetMapping("/findings/{companyId}")
    public ResponseEntity<List<FindingDto>> getFindings(
            @PathVariable UUID companyId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(complianceService.getFindings(companyId, category));
    }

    /**
     * Get latest readiness scores for a company
     */
    @GetMapping("/scores/{companyId}")
    public ResponseEntity<List<ReadinessScoreDto>> getScores(@PathVariable UUID companyId) {
        return ResponseEntity.ok(complianceService.getScores(companyId));
    }

    /**
     * Get score history for before/after tracking
     */
    @GetMapping("/scores/{companyId}/history")
    public ResponseEntity<List<Map<String, Object>>> getScoreHistory(@PathVariable UUID companyId) {
        return ResponseEntity.ok(complianceService.getScoreHistory(companyId));
    }

    /**
     * Mark a finding as resolved
     */
    @PutMapping("/findings/{findingId}/resolve")
    public ResponseEntity<Map<String, String>> resolveFinding(@PathVariable Long findingId) {
        complianceService.resolveFinding(findingId);
        return ResponseEntity.ok(Map.of("status", "resolved"));
    }

    /**
     * Generate gap report for a company
     */
    @GetMapping("/report/{companyId}")
    public ResponseEntity<Map<String, Object>> getGapReport(@PathVariable UUID companyId) {
        return ResponseEntity.ok(gapReportService.generateGapReport(companyId));
    }

    /**
     * Export compliance report as HTML (printable to PDF)
     */
    @GetMapping("/report/{companyId}/pdf")
    public ResponseEntity<String> exportPdf(@PathVariable UUID companyId) {
        String html = pdfExportService.generateReport(companyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=compliance-report.html")
                .body(html);
    }
}
