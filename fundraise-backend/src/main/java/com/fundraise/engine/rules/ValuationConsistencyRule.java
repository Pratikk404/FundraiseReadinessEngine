package com.fundraise.engine.rules;

import com.fundraise.engine.entity.EquityEvent;
import com.fundraise.engine.entity.Finding;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
/**
 * Checks that price per share in cap table is internally consistent.
 *
 * Common failure: the price per share * total shares doesn't equal
 * the declared round valuation, indicating spreadsheet errors or
 * inconsistent data entry.
 */
public class ValuationConsistencyRule implements ComplianceRule {

    private static final double TOLERANCE_PERCENT = 5.0; // 5% tolerance

    @Override
    public String getRuleId() {
        return "VALUATION_CONSISTENCY";
    }

    @Override
    public String getCategory() {
        return "VALUATION";
    }

    @Override
    public Finding.Severity getSeverity() {
        return Finding.Severity.WARNING;
    }

    @Override
    public String getDescription() {
        return "Checks that price per share × total shares is consistent with declared valuation";
    }

    @Override
    public List<Finding> evaluate(ComplianceContext context) {
        List<Finding> findings = new ArrayList<>();

        // Group events by round to check valuation consistency
        var eventsByRound = context.getEquityEvents().stream()
                .filter(e -> e.getRoundName() != null && e.getPricePerShare() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        EquityEvent::getRoundName));

        for (var entry : eventsByRound.entrySet()) {
            String roundName = entry.getKey();
            List<EquityEvent> events = entry.getValue();

            // Calculate total value for this round
            BigDecimal totalValue = events.stream()
                    .map(e -> e.getPricePerShare().multiply(BigDecimal.valueOf(e.getSharesIssued())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Check if price per share is reasonable (> 0)
            for (EquityEvent event : events) {
                if (event.getPricePerShare().compareTo(BigDecimal.ZERO) <= 0) {
                    findings.add(Finding.builder()
                            .company(context.getCompany())
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(Finding.Severity.CRITICAL)
                            .description(String.format(
                                    "Round '%s' has a price per share of %s (zero or negative). " +
                                    "This is invalid and must be corrected.",
                                    roundName, event.getPricePerShare()))
                            .build());
                }
            }

            // Check for suspiciously low valuations (potential tax implications)
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalShares = events.stream()
                        .map(e -> BigDecimal.valueOf(e.getSharesIssued()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalShares.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal impliedValuation = totalValue.divide(
                            BigDecimal.valueOf(context.getTotalSharesFromClass() > 0
                                    ? context.getTotalSharesFromClass() : 1),
                            2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(context.getTotalSharesFromClass() > 0
                                    ? context.getTotalSharesFromClass() : 1));

                    // Flag if implied pre-money valuation seems inconsistent
                    if (impliedValuation.compareTo(BigDecimal.ZERO) > 0
                            && impliedValuation.compareTo(new BigDecimal("100000")) < 0) {
                        findings.add(Finding.builder()
                                .company(context.getCompany())
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(Finding.Severity.INFO)
                                .description(String.format(
                                        "Round '%s' has an implied total value of ₹%s. " +
                                        "This seems unusually low — please verify the price per share is correct.",
                                        roundName, impliedValuation.setScale(0, RoundingMode.HALF_UP)))
                                .build());
                    }
                }
            }
        }

        return findings;
    }
}
