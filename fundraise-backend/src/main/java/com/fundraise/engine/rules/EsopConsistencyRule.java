package com.fundraise.engine.rules;

import com.fundraise.engine.entity.EquityEvent;
import com.fundraise.engine.entity.EsopGrant;
import com.fundraise.engine.entity.Finding;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
/**
 * Checks that ESOP grants are consistent with the cap table.
 *
 * Common failure: founders promise ESOPs verbally or via email,
 * but never reflect them in the cap table or get board approval.
 * During diligence, investors ask for the ESOP register and
 * find mismatches.
 */
public class EsopConsistencyRule implements ComplianceRule {

    @Override
    public String getRuleId() {
        return "ESOP_CONSISTENCY";
    }

    @Override
    public String getCategory() {
        return "ESOP";
    }

    @Override
    public Finding.Severity getSeverity() {
        return Finding.Severity.CRITICAL;
    }

    @Override
    public String getDescription() {
        return "Checks that ESOP grants match the cap table and have board approval";
    }

    @Override
    public List<Finding> evaluate(ComplianceContext context) {
        List<Finding> findings = new ArrayList<>();

        List<EsopGrant> informalGrants = context.getInformalEsopGrants();
        if (!informalGrants.isEmpty()) {
            long informalShares = informalGrants.stream().mapToLong(EsopGrant::getShares).sum();
            String grantees = informalGrants.stream()
                    .map(EsopGrant::getGrantee)
                    .filter(g -> g != null)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("unknown");

            findings.add(Finding.builder()
                    .company(context.getCompany())
                    .ruleId(getRuleId())
                    .category(getCategory())
                    .severity(getSeverity())
                    .description(String.format(
                            "%d ESOP grant(s) totaling %,d shares are NOT board-approved. " +
                            "Grantees: %s. These grants must receive board resolution " +
                            "before fundraise diligence.",
                            informalGrants.size(), informalShares, grantees))
                    .build());
        }

        // Check if total ESOP grants exceed any declared ESOP pool in cap table
        long totalEsopFromGrants = context.getTotalEsopShares();
        long totalEsopFromCapTable = context.getEquityEvents().stream()
                .filter(e -> e.getInstrumentType() == EquityEvent.InstrumentType.ESOP_POOL)
                .mapToLong(EquityEvent::getSharesIssued)
                .sum();

        if (totalEsopFromCapTable > 0 && totalEsopFromGrants > totalEsopFromCapTable) {
            findings.add(Finding.builder()
                    .company(context.getCompany())
                    .ruleId(getRuleId())
                    .category(getCategory())
                    .severity(Finding.Severity.WARNING)
                    .description(String.format(
                            "ESOP grants (%,d shares) exceed the ESOP pool declared in cap table (%,d shares). " +
                            "This indicates either unauthorized grants or an insufficient pool allocation.",
                            totalEsopFromGrants, totalEsopFromCapTable))
                    .build());
        }

        return findings;
    }
}
