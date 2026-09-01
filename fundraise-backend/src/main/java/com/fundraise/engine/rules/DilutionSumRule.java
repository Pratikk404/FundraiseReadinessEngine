package com.fundraise.engine.rules;

import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.Finding;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
/**
 * Checks that total equity across all events sums to exactly 100%.
 *
 * Common failure: founders forget ESOP top-ups, or spreadsheet drift
 * causes cumulative issuance to not match total authorized shares.
 *
 * This is the #1 most common cap table error in Indian startups.
 */
public class DilutionSumRule implements ComplianceRule {

    private static final double TOLERANCE = 0.01; // 0.01% tolerance for rounding

    @Override
    public String getRuleId() {
        return "DILUTION_SUM_100";
    }

    @Override
    public String getCategory() {
        return "CAP_TABLE";
    }

    @Override
    public Finding.Severity getSeverity() {
        return Finding.Severity.CRITICAL;
    }

    @Override
    public String getDescription() {
        return "Verifies that total equity across all events sums to 100% of authorized shares";
    }

    @Override
    public List<Finding> evaluate(ComplianceContext context) {
        List<Finding> findings = new ArrayList<>();

        long totalIssued = context.getTotalSharesIssued();
        long totalFromClass = context.getTotalSharesFromClass();

        // If we have share class data, compare against it
        if (totalFromClass > 0) {
            double percentage = (double) totalIssued / totalFromClass * 100;
            double deviation = Math.abs(percentage - 100.0);

            if (deviation > TOLERANCE) {
                findings.add(Finding.builder()
                        .company(context.getCompany())
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .description(String.format(
                                "Equity issuance sums to %.2f%% of authorized shares (expected 100%%). " +
                                "Total issued: %,d shares, Authorized: %,d shares. " +
                                "This indicates spreadsheet drift or missing ESOP top-ups.",
                                percentage, totalIssued, totalFromClass))
                        .build());
            }
        }

        // Check for ESOP promises not reflected in cap table
        long informalEsop = context.getInformalEsopGrants().size();
        if (informalEsop > 0) {
            long informalShares = context.getInformalEsopGrants().stream()
                    .mapToLong(g -> g.getShares())
                    .sum();

            findings.add(Finding.builder()
                    .company(context.getCompany())
                    .ruleId(getRuleId())
                    .category(getCategory())
                    .severity(Finding.Severity.WARNING)
                    .description(String.format(
                            "%d informal ESOP grant(s) totaling %,d shares are not reflected in the cap table. " +
                            "These promises must be formalized before fundraise.",
                            informalEsop, informalShares))
                    .build());
        }

        return findings;
    }
}
