package com.fundraise.engine.rules;

import com.fundraise.engine.entity.Finding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The Rules Engine orchestrates all compliance rules.
 * It discovers all ComplianceRule beans and evaluates them against the context.
 *
 * Adding a new rule = creating a new @Component class implementing ComplianceRule.
 * No changes to this engine are needed.
 */
@Component
@Slf4j
public class RulesEngine {

    private final List<ComplianceRule> rules;

    public RulesEngine(List<ComplianceRule> rules) {
        this.rules = rules;
        log.info("Rules Engine initialized with {} rules: {}",
                rules.size(),
                rules.stream().map(ComplianceRule::getRuleId).toList());
    }

    /**
     * Evaluate all rules against the given context.
     * Returns a complete list of findings (may be empty if all checks pass).
     */
    public List<Finding> evaluateAll(ComplianceContext context) {
        List<Finding> allFindings = new ArrayList<>();

        for (ComplianceRule rule : rules) {
            try {
                log.debug("Evaluating rule: {} ({})", rule.getRuleId(), rule.getDescription());
                List<Finding> findings = rule.evaluate(context);
                allFindings.addAll(findings);
                log.debug("Rule {} produced {} finding(s)", rule.getRuleId(), findings.size());
            } catch (Exception e) {
                log.error("Error evaluating rule {}: {}", rule.getRuleId(), e.getMessage(), e);
                // Add an internal error finding so the user knows this rule couldn't run
                allFindings.add(Finding.builder()
                        .company(context.getCompany())
                        .ruleId(rule.getRuleId())
                        .category(rule.getCategory())
                        .severity(Finding.Severity.INFO)
                        .description(String.format(
                                "Rule '%s' could not be evaluated due to an internal error: %s",
                                rule.getRuleId(), e.getMessage()))
                        .build());
            }
        }

        log.info("Rules evaluation complete: {} total findings from {} rules",
                allFindings.size(), rules.size());

        return allFindings;
    }

    /**
     * Get all registered rules (for dashboard display)
     */
    public List<ComplianceRule> getRules() {
        return List.copyOf(rules);
    }
}
