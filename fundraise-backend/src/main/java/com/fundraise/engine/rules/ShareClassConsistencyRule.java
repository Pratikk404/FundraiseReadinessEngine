package com.fundraise.engine.rules;

import com.fundraise.engine.entity.EquityEvent;
import com.fundraise.engine.entity.Finding;
import com.fundraise.engine.entity.ShareClass;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
/**
 * Checks that share classes declared in incorporation documents
 * are consistent with what's in the cap table.
 *
 * Common failure: side letters promise different rights than
 * the official share class structure, or new classes are created
 * without updating incorporation docs.
 */
public class ShareClassConsistencyRule implements ComplianceRule {

    @Override
    public String getRuleId() {
        return "SHARE_CLASS_CONSISTENCY";
    }

    @Override
    public String getCategory() {
        return "SHARE_STRUCTURE";
    }

    @Override
    public Finding.Severity getSeverity() {
        return Finding.Severity.WARNING;
    }

    @Override
    public String getDescription() {
        return "Checks that share classes in cap table match incorporation documents";
    }

    @Override
    public List<Finding> evaluate(ComplianceContext context) {
        List<Finding> findings = new ArrayList<>();

        // Get share classes from documents
        Map<String, Long> classShares = context.getShareClasses().stream()
                .filter(sc -> sc.getTotalShares() != null)
                .collect(Collectors.toMap(
                        ShareClass::getClassName,
                        ShareClass::getTotalShares,
                        Long::sum));

        // Get share types used in equity events
        Map<EquityEvent.InstrumentType, Long> eventShares = context.getEquityEvents().stream()
                .collect(Collectors.groupingBy(
                        EquityEvent::getInstrumentType,
                        Collectors.summingLong(EquityEvent::getSharesIssued)));

        // Check if total from share classes matches total from events
        long totalFromClass = classShares.values().stream().mapToLong(Long::longValue).sum();
        long totalFromEvents = eventShares.values().stream().mapToLong(Long::longValue).sum();

        if (totalFromClass > 0 && totalFromEvents > 0 && totalFromClass != totalFromEvents) {
            findings.add(Finding.builder()
                    .company(context.getCompany())
                    .ruleId(getRuleId())
                    .category(getCategory())
                    .severity(Finding.Severity.WARNING)
                    .description(String.format(
                            "Total shares from share classes (%,d) does not match total from equity events (%,d). " +
                            "This indicates inconsistency between incorporation documents and cap table.",
                            totalFromClass, totalFromEvents))
                    .build());
        }

        // Check for share classes declared in documents but not used in events
        for (ShareClass sc : context.getShareClasses()) {
            if (!eventShares.containsKey(instrumentTypeFromClass(sc.getClassName()))) {
                findings.add(Finding.builder()
                        .company(context.getCompany())
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(Finding.Severity.INFO)
                        .description(String.format(
                                "Share class '%s' is declared in documents but has no corresponding " +
                                "equity events in the cap table.",
                                sc.getClassName()))
                        .build());
            }
        }

        return findings;
    }

    private EquityEvent.InstrumentType instrumentTypeFromClass(String className) {
        return switch (className.toUpperCase()) {
            case "ORDINARY", "COMMON" -> EquityEvent.InstrumentType.COMMON;
            case "PREFERRED_A", "PREF_A" -> EquityEvent.InstrumentType.PREFERRED_A;
            case "PREFERRED_B", "PREF_B" -> EquityEvent.InstrumentType.PREFERRED_B;
            default -> EquityEvent.InstrumentType.COMMON;
        };
    }
}
