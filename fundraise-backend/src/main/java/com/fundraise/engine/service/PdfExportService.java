package com.fundraise.engine.service;

import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.Finding;
import com.fundraise.engine.repository.CompanyRepository;
import com.fundraise.engine.repository.FindingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates PDF compliance reports.
 * For MVP, returns HTML that can be printed to PDF by the browser.
 * Can be upgraded to iText/OpenPDF for server-side PDF generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private final CompanyRepository companyRepository;
    private final FindingRepository findingRepository;

    /**
     * Generate HTML report that can be printed to PDF
     */
    public String generateReport(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        List<Finding> findings = findingRepository.findByCompanyId(companyId);
        List<Finding> unresolved = findings.stream()
                .filter(f -> !f.isResolved())
                .toList();

        long criticalCount = unresolved.stream()
                .filter(f -> f.getSeverity() == Finding.Severity.CRITICAL).count();
        long warningCount = unresolved.stream()
                .filter(f -> f.getSeverity() == Finding.Severity.WARNING).count();

        String readiness;
        if (criticalCount == 0 && warningCount == 0) {
            readiness = "<span style=\"color:#16a34a\">✅ READY FOR FUNDRAISE</span>";
        } else if (criticalCount == 0) {
            readiness = "<span style=\"color:#ca8a04\">⚠️ NEAR READY</span>";
        } else {
            readiness = "<span style=\"color:#dc2626\">❌ NOT READY</span>";
        }

        Map<String, List<Finding>> byCategory = unresolved.stream()
                .collect(Collectors.groupingBy(Finding::getCategory, LinkedHashMap::new, Collectors.toList()));

        StringBuilder html = new StringBuilder();
        html.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Compliance Report - %s</title>
                    <style>
                        body { font-family: -apple-system, system-ui, sans-serif; max-width: 800px; margin: 0 auto; padding: 40px 20px; color: #1f2937; }
                        h1 { color: #111827; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px; }
                        h2 { color: #374151; margin-top: 32px; }
                        .header { text-align: center; margin-bottom: 40px; }
                        .logo { font-size: 28px; font-weight: bold; color: #4f46e5; }
                        .summary { display: flex; gap: 16px; margin: 20px 0; }
                        .summary-card { flex: 1; padding: 16px; border-radius: 8px; text-align: center; }
                        .critical { background: #fef2f2; border: 1px solid #fecaca; }
                        .warning { background: #fffbeb; border: 1px solid #fde68a; }
                        .info { background: #eff6ff; border: 1px solid #bfdbfe; }
                        .finding { padding: 12px 16px; margin: 8px 0; border-radius: 6px; border-left: 4px solid; }
                        .finding-critical { border-color: #dc2626; background: #fef2f2; }
                        .finding-warning { border-color: #ca8a04; background: #fffbeb; }
                        .finding-info { border-color: #2563eb; background: #eff6ff; }
                        .finding-rule { font-weight: 600; font-size: 14px; }
                        .finding-desc { margin-top: 4px; font-size: 14px; color: #4b5563; }
                        .priority { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 16px; margin-top: 20px; }
                        .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #9ca3af; border-top: 1px solid #e5e7eb; padding-top: 16px; }
                        @media print { body { padding: 20px; } }
                    </style>
                </head>
                <body>
                """);

        html.append(String.format("""
                    <div class="header">
                        <div class="logo">FundraiseReady</div>
                        <h1>Compliance Gap Report</h1>
                        <p><strong>%s</strong></p>
                        <p>Generated: %s</p>
                        <p>%s</p>
                    </div>
                """, company.getName(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")),
                readiness));

        // Summary cards
        html.append("""
                <div class="summary">
                    <div class="summary-card critical">
                        <div style="font-size:32px;font-weight:bold;color:#dc2626\">%d</div>
                        <div style="font-size:14px;color:#7f1d1d\">Critical</div>
                    </div>
                    <div class="summary-card warning">
                        <div style="font-size:32px;font-weight:bold;color:#ca8a04\">%d</div>
                        <div style="font-size:14px;color:#78350f\">Warnings</div>
                    </div>
                    <div class="summary-card info">
                        <div style="font-size:32px;font-weight:bold;color:#2563eb\">%d</div>
                        <div style="font-size:14px;color:#1e3a5f\">Info</div>
                    </div>
                </div>
                """.formatted(criticalCount, warningCount,
                unresolved.stream().filter(f -> f.getSeverity() == Finding.Severity.INFO).count()));

        // Findings by category
        if (!byCategory.isEmpty()) {
            html.append("<h2>Findings</h2>");

            for (var entry : byCategory.entrySet()) {
                String category = formatCategory(entry.getKey());
                html.append(String.format("<h3>%s</h3>", category));

                for (Finding f : entry.getValue()) {
                    String cssClass = switch (f.getSeverity()) {
                        case CRITICAL -> "finding-critical";
                        case WARNING -> "finding-warning";
                        case INFO -> "finding-info";
                    };
                    String icon = switch (f.getSeverity()) {
                        case CRITICAL -> "🔴";
                        case WARNING -> "🟡";
                        case INFO -> "🔵";
                    };

                    html.append(String.format("""
                            <div class="finding %s">
                                <div class="finding-rule">%s %s</div>
                                <div class="finding-desc">%s</div>
                            </div>
                            """, cssClass, icon, f.getRuleId(), f.getDescription()));
                }
            }
        }

        // Priority actions
        List<Finding> criticals = unresolved.stream()
                .filter(f -> f.getSeverity() == Finding.Severity.CRITICAL)
                .limit(3)
                .toList();

        if (!criticals.isEmpty()) {
            html.append("""
                    <div class="priority">
                        <h3 style="margin-top:0">🎯 Priority Actions</h3>
                        <ol>
                    """);
            for (Finding f : criticals) {
                html.append(String.format("<li><strong>%s:</strong> %s</li>", f.getRuleId(), getActionForRule(f.getRuleId())));
            }
            html.append("</ol></div>");
        }

        // Footer
        html.append("""
                <div class="footer">
                    <p>Generated by FundraiseReady — Document-driven compliance diagnostic for Indian startups</p>
                    <p>This report is for informational purposes. Consult a legal advisor for formal compliance certification.</p>
                </div>
                </body></html>
                """);

        return html.toString();
    }

    private String getActionForRule(String ruleId) {
        return switch (ruleId) {
            case "DILUTION_SUM_100" -> "Reconcile your cap table so total equity sums to exactly 100%";
            case "DPIIT_RECOGNITION" -> "Apply for DPIIT recognition at startupindia.gov.in";
            case "ESOP_CONSISTENCY" -> "Formalize all ESOP promises with board resolutions";
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
}
