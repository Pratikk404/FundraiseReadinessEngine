package com.fundraise.engine.controller;

import com.fundraise.engine.dto.FindingDto;
import com.fundraise.engine.dto.FindingGuideDto;
import com.fundraise.engine.dto.ReadinessScoreDto;
import com.fundraise.engine.service.ComplianceService;
import com.fundraise.engine.service.FindingGuideService;
import com.fundraise.engine.service.GapReportService;
import com.fundraise.engine.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Compliance", description = "Run compliance checks, view findings, and generate reports")
public class ComplianceController {

    private final ComplianceService complianceService;
    private final GapReportService gapReportService;
    private final PdfExportService pdfExportService;
    private final FindingGuideService findingGuideService;

    @PostMapping("/check/{companyId}")
    @Operation(summary = "Run compliance check", description = "Execute all 5 compliance rules against the company's documents and data. Returns findings and readiness scores.")
    public ResponseEntity<Map<String, Object>> runComplianceCheck(@PathVariable UUID companyId) {
        return ResponseEntity.ok(complianceService.runComplianceCheck(companyId));
    }

    @GetMapping("/findings/{companyId}")
    @Operation(summary = "Get findings", description = "List all compliance findings for a company. Optionally filter by category.")
    public ResponseEntity<List<FindingDto>> getFindings(
            @PathVariable UUID companyId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(complianceService.getFindings(companyId, category));
    }

    @GetMapping("/scores/{companyId}")
    @Operation(summary = "Get readiness scores", description = "Latest readiness scores by category (CAP_TABLE, DPIIT, ESOP, etc).")
    public ResponseEntity<List<ReadinessScoreDto>> getScores(@PathVariable UUID companyId) {
        return ResponseEntity.ok(complianceService.getScores(companyId));
    }

    @GetMapping("/scores/{companyId}/history")
    @Operation(summary = "Score history", description = "Before/after score tracking across multiple compliance check runs.")
    public ResponseEntity<List<Map<String, Object>>> getScoreHistory(@PathVariable UUID companyId) {
        return ResponseEntity.ok(complianceService.getScoreHistory(companyId));
    }

    @PutMapping("/findings/{findingId}/resolve")
    @Operation(summary = "Resolve finding", description = "Mark a compliance finding as resolved.")
    public ResponseEntity<Map<String, String>> resolveFinding(@PathVariable Long findingId) {
        complianceService.resolveFinding(findingId);
        return ResponseEntity.ok(Map.of("status", "resolved"));
    }

    @GetMapping("/report/{companyId}")
    @Operation(summary = "Gap report", description = "AI-powered gap report with severity breakdown, priority actions, and narrative.")
    public ResponseEntity<Map<String, Object>> getGapReport(@PathVariable UUID companyId) {
        return ResponseEntity.ok(gapReportService.generateGapReport(companyId));
    }

    @GetMapping("/report/{companyId}/pdf")
    @Operation(summary = "Export report as HTML", description = "Printable HTML compliance report. Open in browser and print to PDF.")
    public ResponseEntity<String> exportPdf(@PathVariable UUID companyId) {
        String html = pdfExportService.generateReport(companyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=compliance-report.html")
                .body(html);
    }

    @GetMapping("/guides")
    @Operation(summary = "All fix guides", description = "Get fix-it guidance for all compliance rules.")
    public ResponseEntity<Map<String, FindingGuideDto>> getAllGuides() {
        return ResponseEntity.ok(findingGuideService.getAllGuides());
    }

    @GetMapping("/guides/{ruleId}")
    @Operation(summary = "Fix guide for rule", description = "Get step-by-step fix guidance for a specific compliance rule.")
    public ResponseEntity<FindingGuideDto> getGuideForRule(@PathVariable String ruleId) {
        return findingGuideService.getGuideForRule(ruleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
