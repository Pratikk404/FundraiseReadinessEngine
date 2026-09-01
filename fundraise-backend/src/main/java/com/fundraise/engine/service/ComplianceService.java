package com.fundraise.engine.service;

import com.fundraise.engine.dto.FindingDto;
import com.fundraise.engine.dto.ReadinessScoreDto;
import com.fundraise.engine.entity.*;
import com.fundraise.engine.repository.*;
import com.fundraise.engine.rules.ComplianceContext;
import com.fundraise.engine.rules.RulesEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceService {

    private final RulesEngine rulesEngine;
    private final EquityEventRepository equityEventRepository;
    private final EsopGrantRepository esopGrantRepository;
    private final ShareClassRepository shareClassRepository;
    private final DocumentRepository documentRepository;
    private final FindingRepository findingRepository;
    private final ReadinessScoreRepository readinessScoreRepository;

    /**
     * Run all compliance rules for a company.
     * Clears existing findings and generates fresh ones.
     */
    @Transactional
    public Map<String, Object> runComplianceCheck(UUID companyId) {
        log.info("Running compliance check for company: {}", companyId);

        // Build context
        List<EquityEvent> equityEvents = equityEventRepository.findByCompanyIdOrderByEventDateAsc(companyId);
        List<EsopGrant> esopGrants = esopGrantRepository.findByCompanyId(companyId);
        List<ShareClass> shareClasses = shareClassRepository.findByCompanyId(companyId);
        List<Document> documents = documentRepository.findByCompanyId(companyId);

        Company company = equityEvents.isEmpty()
                ? null
                : equityEvents.get(0).getCompany();

        if (company == null) {
            // Try to get company from documents
            company = documents.isEmpty() ? null : documents.get(0).getCompany();
        }

        if (company == null) {
            throw new IllegalArgumentException("No data found for company: " + companyId);
        }

        ComplianceContext context = ComplianceContext.builder()
                .company(company)
                .equityEvents(equityEvents)
                .esopGrants(esopGrants)
                .shareClasses(shareClasses)
                .documents(documents)
                .build();

        // Clear old findings
        findingRepository.findByCompanyId(companyId).stream()
                .map(Finding::getId)
                .forEach(findingRepository::deleteById);

        // Run all rules
        List<Finding> findings = rulesEngine.evaluateAll(context);

        // Save findings
        List<Finding> savedFindings = findingRepository.saveAll(findings);

        // Compute scores by category
        Map<String, ReadinessScore> scores = computeScores(companyId, savedFindings);
        readinessScoreRepository.saveAll(scores.values());

        // Build response
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("companyId", companyId);
        result.put("companyName", company.getName());
        result.put("totalFindings", savedFindings.size());
        result.put("criticalCount", savedFindings.stream()
                .filter(f -> f.getSeverity() == Finding.Severity.CRITICAL).count());
        result.put("warningCount", savedFindings.stream()
                .filter(f -> f.getSeverity() == Finding.Severity.WARNING).count());
        result.put("infoCount", savedFindings.stream()
                .filter(f -> f.getSeverity() == Finding.Severity.INFO).count());
        result.put("findings", savedFindings.stream()
                .map(FindingDto::fromEntity).toList());
        result.put("scores", scores.values().stream()
                .map(s -> ReadinessScoreDto.builder()
                        .category(s.getCategory())
                        .score(s.getScore())
                        .computedAt(s.getComputedAt())
                        .build())
                .toList());

        log.info("Compliance check complete: {} findings, {} scores",
                savedFindings.size(), scores.size());

        return result;
    }

    /**
     * Get findings for a company
     */
    public List<FindingDto> getFindings(UUID companyId, String category) {
        List<Finding> findings;
        if (category != null && !category.isEmpty()) {
            findings = findingRepository.findByCompanyIdAndCategory(companyId, category);
        } else {
            findings = findingRepository.findByCompanyId(companyId);
        }
        return findings.stream().map(FindingDto::fromEntity).toList();
    }

    /**
     * Get readiness scores for a company
     */
    public List<ReadinessScoreDto> getScores(UUID companyId) {
        return readinessScoreRepository.findByCompanyIdOrderByComputedAtDesc(companyId).stream()
                .map(s -> ReadinessScoreDto.builder()
                        .category(s.getCategory())
                        .score(s.getScore())
                        .computedAt(s.getComputedAt())
                        .build())
                .toList();
    }

    /**
     * Mark a finding as resolved
     */
    @Transactional
    public void resolveFinding(Long findingId) {
        Finding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new IllegalArgumentException("Finding not found: " + findingId));
        finding.setResolved(true);
        findingRepository.save(finding);
    }

    /**
     * Compute readiness scores by category
     */
    private Map<String, ReadinessScore> computeScores(UUID companyId, List<Finding> findings) {
        Map<String, ReadinessScore> scores = new LinkedHashMap<>();

        // Group findings by category
        Map<String, List<Finding>> byCategory = findings.stream()
                .collect(Collectors.groupingBy(Finding::getCategory));

        // Define category weights
        Map<String, BigDecimal> categoryWeights = Map.of(
                "CAP_TABLE", new BigDecimal("30"),
                "DPIIT", new BigDecimal("25"),
                "ESOP", new BigDecimal("20"),
                "SHARE_STRUCTURE", new BigDecimal("15"),
                "VALUATION", new BigDecimal("10")
        );

        // Base score per category
        BigDecimal baseScore = new BigDecimal("100");

        for (var entry : categoryWeights.entrySet()) {
            String category = entry.getKey();
            List<Finding> categoryFindings = byCategory.getOrDefault(category, List.of());

            // Deductions
            BigDecimal deduction = BigDecimal.ZERO;
            for (Finding f : categoryFindings) {
                if (!f.isResolved()) {
                    deduction = switch (f.getSeverity()) {
                        case CRITICAL -> deduction.add(new BigDecimal("25"));
                        case WARNING -> deduction.add(new BigDecimal("10"));
                        case INFO -> deduction.add(new BigDecimal("2"));
                    };
                }
            }

            BigDecimal score = baseScore.subtract(deduction).max(BigDecimal.ZERO);
            if (score.compareTo(new BigDecimal("100")) > 0) {
                score = new BigDecimal("100");
            }

            scores.put(category, ReadinessScore.builder()
                    .company(companyId != null
                            ? findingRepository.findById(0L).map(Finding::getCompany).orElse(null)
                            : null)
                    .category(category)
                    .score(score.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return scores;
    }
}
