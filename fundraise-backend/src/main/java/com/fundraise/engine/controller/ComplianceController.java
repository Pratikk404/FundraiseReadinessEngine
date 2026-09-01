package com.fundraise.engine.controller;

import com.fundraise.engine.dto.FindingDto;
import com.fundraise.engine.dto.ReadinessScoreDto;
import com.fundraise.engine.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
     * Get readiness scores for a company
     */
    @GetMapping("/scores/{companyId}")
    public ResponseEntity<List<ReadinessScoreDto>> getScores(@PathVariable UUID companyId) {
        return ResponseEntity.ok(complianceService.getScores(companyId));
    }

    /**
     * Mark a finding as resolved
     */
    @PutMapping("/findings/{findingId}/resolve")
    public ResponseEntity<Map<String, String>> resolveFinding(@PathVariable Long findingId) {
        complianceService.resolveFinding(findingId);
        return ResponseEntity.ok(Map.of("status", "resolved"));
    }
}
