package com.fundraise.engine.service;

import com.fundraise.engine.dto.FindingDto;
import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.Finding;
import com.fundraise.engine.repository.CompanyRepository;
import com.fundraise.engine.repository.FindingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a founder-readable gap report from rule-engine findings.
 * Uses LLM API (OpenAI/Anthropic) for narrative generation,
 * with a deterministic fallback for offline/demo mode.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GapReportService {

    private final FindingRepository findingRepository;
    private final CompanyRepository companyRepository;

    @Value("${app.llm.api-key:}")
    private String apiKey;

    @Value("${app.llm.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${app.llm.model:gpt-4o-mini}")
    private String model;

    /**
     * Generate a gap report for a company
     */
    public Map<String, Object> generateGapReport(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        List<Finding> findings = findingRepository.findByCompanyId(companyId);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("companyId", companyId);
        report.put("companyName", company.getName());
        report.put("generatedAt", LocalDateTime.now());
        report.put("totalFindings", findings.size());

        // Severity summary
        Map<String, Long> severitySummary = findings.stream()
                .filter(f -> !f.isResolved())
                .collect(Collectors.groupingBy(
                        f -> f.getSeverity().name(),
                        LinkedHashMap::new,
                        Collectors.counting()));
        report.put("severitySummary", severitySummary);

        // Category breakdown
        Map<String, List<FindingDto>> byCategory = findings.stream()
                .filter(f -> !f.isResolved())
                .map(FindingDto::fromEntity)
                .collect(Collectors.groupingBy(
                        FindingDto::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()));
        report.put("categoryBreakdown", byCategory);

        // Priority actions (top 3 critical findings)
        List<Map<String, String>> priorityActions = findings.stream()
                .filter(f -> !f.isResolved() && f.getSeverity() == Finding.Severity.CRITICAL)
                .limit(3)
                .map(f -> {
                    Map<String, String> action = new LinkedHashMap<>();
                    action.put("ruleId", f.getRuleId());
                    action.put("category", f.getCategory());
                    action.put("issue", f.getDescription());
                    action.put("action", getActionForRule(f.getRuleId()));
                    return action;
                })
                .toList();
        report.put("priorityActions", priorityActions);

        // Generate narrative
        String narrative = generateNarrative(company, findings);
        report.put("narrative", narrative);

        // Overall readiness
        long criticalCount = findings.stream()
                .filter(f -> !f.isResolved() && f.getSeverity() == Finding.Severity.CRITICAL)
                .count();
        long warningCount = findings.stream()
                .filter(f -> !f.isResolved() && f.getSeverity() == Finding.Severity.WARNING)
                .count();

        String readiness;
        if (criticalCount == 0 && warningCount == 0) {
            readiness = "READY";
        } else if (criticalCount == 0) {
            readiness = "NEAR_READY";
        } else {
            readiness = "NOT_READY";
        }
        report.put("readiness", readiness);

        log.info("Gap report generated for {}: {} findings, readiness={}",
                company.getName(), findings.size(), readiness);

        return report;
    }

    /**
     * Generate narrative text using LLM or deterministic fallback
     */
    private String generateNarrative(Company company, List<Finding> findings) {
        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                return generateWithLLM(company, findings);
            } catch (Exception e) {
                log.warn("LLM generation failed, falling back to deterministic: {}", e.getMessage());
            }
        }
        return generateDeterministic(company, findings);
    }

    /**
     * Call LLM API for narrative generation
     */
    private String generateWithLLM(Company company, List<Finding> findings) {
        String findingsJson = findings.stream()
                .filter(f -> !f.isResolved())
                .map(f -> String.format("{\"rule\":\"%s\",\"category\":\"%s\",\"severity\":\"%s\",\"description\":\"%s\"}",
                        f.getRuleId(), f.getCategory(), f.getSeverity(), escapeJson(f.getDescription())))
                .collect(Collectors.joining(",\n"));

        String prompt = String.format("""
                You are a compliance advisor for Indian startups. Based on the following rule-engine findings,
                generate a founder-readable gap report for %s.

                ## Findings
                [%s]

                ## Requirements
                - Group by category (Cap Table, DPIIT, FEMA, ESOP, Share Structure, Valuation)
                - Use plain language, not legal jargon
                - For each finding, explain WHY it matters for fundraising
                - End with a "Priority Actions" section (top 3 things to fix first)
                - Keep it under 500 words
                - Be direct and actionable
                """, company.getName(), findingsJson);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 1000,
                "temperature", 0.3
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        if (response.getBody() != null && response.getBody().get("choices") != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }

        return generateDeterministic(company, findings);
    }

    /**
     * Deterministic fallback when LLM is unavailable
     */
    private String generateDeterministic(Company company, List<Finding> findings) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Compliance Gap Report\n\n");
        sb.append("**Company:** ").append(company.getName()).append("\n");
        sb.append("**Date:** ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))).append("\n\n");

        long unresolved = findings.stream().filter(f -> !f.isResolved()).count();
        if (unresolved == 0) {
            sb.append("## ✅ All Clear\n\n");
            sb.append("No unresolved compliance issues found. Your company is ready for fundraise diligence.\n");
            return sb.toString();
        }

        sb.append("## Summary\n\n");
        sb.append(String.format("- **%d** unresolved findings\n", unresolved));
        sb.append(String.format("- **%d** critical issues (must fix before fundraise)\n",
                findings.stream().filter(f -> !f.isResolved() && f.getSeverity() == Finding.Severity.CRITICAL).count()));
        sb.append(String.format("- **%d** warnings (should fix)\n",
                findings.stream().filter(f -> !f.isResolved() && f.getSeverity() == Finding.Severity.WARNING).count()));
        sb.append("\n");

        // Group by category
        Map<String, List<Finding>> byCategory = findings.stream()
                .filter(f -> !f.isResolved())
                .collect(Collectors.groupingBy(Finding::getCategory, LinkedHashMap::new, Collectors.toList()));

        for (var entry : byCategory.entrySet()) {
            String category = formatCategory(entry.getKey());
            sb.append("## ").append(category).append("\n\n");

            for (Finding f : entry.getValue()) {
                String icon = switch (f.getSeverity()) {
                    case CRITICAL -> "🔴";
                    case WARNING -> "🟡";
                    case INFO -> "🔵";
                };
                sb.append(icon).append(" **").append(f.getRuleId()).append("**\n");
                sb.append(f.getDescription()).append("\n\n");
            }
        }

        // Priority actions
        List<Finding> criticals = findings.stream()
                .filter(f -> !f.isResolved() && f.getSeverity() == Finding.Severity.CRITICAL)
                .toList();

        if (!criticals.isEmpty()) {
            sb.append("## 🎯 Priority Actions\n\n");
            sb.append("Fix these before your fundraise:\n\n");
            int i = 1;
            for (Finding f : criticals) {
                sb.append(i).append(". ").append(getActionForRule(f.getRuleId())).append("\n");
                i++;
                if (i > 3) break;
            }
        }

        return sb.toString();
    }

    private String getActionForRule(String ruleId) {
        return switch (ruleId) {
            case "DILUTION_SUM_100" -> "Reconcile your cap table so total equity sums to exactly 100%";
            case "DPIIT_RECOGNITION" -> "Apply for DPIIT recognition at startupindia.gov.in";
            case "ESOP_CONSISTENCY" -> "Formalize all ESOP promises with board resolutions and update the cap table";
            case "SHARE_CLASS_CONSISTENCY" -> "Align share classes between incorporation docs and cap table";
            case "VALUATION_CONSISTENCY" -> "Verify price per share matches declared round valuation";
            default -> "Review and resolve this finding";
        };
    }

    private String formatCategory(String category) {
        return switch (category) {
            case "CAP_TABLE" -> "📊 Cap Table";
            case "DPIIT" -> "🏛️ DPIIT Recognition";
            case "ESOP" -> "👥 ESOP";
            case "SHARE_STRUCTURE" -> "📋 Share Structure";
            case "VALUATION" -> "💰 Valuation";
            default -> category;
        };
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
