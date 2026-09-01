package com.fundraise.engine.rules;

import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.Finding;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
/**
 * Checks that the company has valid DPIIT recognition.
 *
 * Missing DPIIT recognition exposes the company to angel tax under
 * Section 56(2)(viib) of the Income Tax Act — investors will flag
 * this as a deal blocker during diligence.
 *
 * Also checks if recognition has lapsed (valid for 10 years from incorporation).
 */
public class DpiitRecognitionRule implements ComplianceRule {

    @Override
    public String getRuleId() {
        return "DPIIT_RECOGNITION";
    }

    @Override
    public String getCategory() {
        return "DPIIT";
    }

    @Override
    public Finding.Severity getSeverity() {
        return Finding.Severity.CRITICAL;
    }

    @Override
    public String getDescription() {
        return "Verifies DPIIT recognition status and Section 56(2)(viib) angel tax exposure";
    }

    @Override
    public List<Finding> evaluate(ComplianceContext context) {
        List<Finding> findings = new ArrayList<>();
        Company company = context.getCompany();

        switch (company.getDpiitStatus()) {
            case NOT_RECOGNIZED -> {
                findings.add(Finding.builder()
                        .company(company)
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(Finding.Severity.CRITICAL)
                        .description(String.format(
                                "Company '%s' does NOT have DPIIT recognition. " +
                                "This exposes the company to angel tax under Section 56(2)(viib) " +
                                "on any share premium received above face value. " +
                                "Investors will require this to be resolved before closing.",
                                company.getName()))
                        .build());
            }
            case UNKNOWN -> {
                findings.add(Finding.builder()
                        .company(company)
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(Finding.Severity.WARNING)
                        .description(String.format(
                                "DPIIT recognition status for '%s' is UNKNOWN. " +
                                "Please verify DPIIT recognition at https://www.startupindia.gov.in/ " +
                                "and update the company profile.",
                                company.getName()))
                        .build());
            }
            case RECOGNIZED -> {
                // Check if recognition might have lapsed
                if (company.getDpiitRecognitionDate() != null) {
                    var yearsSinceRecognition = java.time.temporal.ChronoUnit.YEARS.between(
                            company.getDpiitRecognitionDate(), java.time.LocalDate.now());
                    if (yearsSinceRecognition > 10) {
                        findings.add(Finding.builder()
                                .company(company)
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(Finding.Severity.WARNING)
                                .description(String.format(
                                        "DPIIT recognition for '%s' was granted on %s (%d years ago). " +
                                        "Recognition is valid for 10 years from incorporation. " +
                                        "Please verify it has not lapsed.",
                                        company.getName(),
                                        company.getDpiitRecognitionDate(),
                                        yearsSinceRecognition))
                                .build());
                    }
                }
            }
        }

        return findings;
    }
}
