package com.fundraise.engine.rules;

import com.fundraise.engine.entity.Finding;

import java.util.List;

/**
 * Core interface for all compliance rules.
 * Each rule is independently testable and produces findings.
 *
 * This is the most important abstraction in the system —
 * adding a new compliance check means implementing this interface,
 * not modifying a monolithic scoring function.
 */
public interface ComplianceRule {

    /**
     * Unique identifier for this rule (e.g., "DILUTION_SUM_100")
     */
    String getRuleId();

    /**
     * Category this rule belongs to (e.g., "CAP_TABLE", "DPIIT", "FEMA", "ESOP")
     */
    String getCategory();

    /**
     * Default severity if the rule is violated
     */
    Finding.Severity getSeverity();

    /**
     * Human-readable description of what this rule checks
     */
    String getDescription();

    /**
     * Evaluate this rule against the given context.
     * Returns an empty list if no issues found.
     * Returns one or more findings if issues are detected.
     */
    List<Finding> evaluate(ComplianceContext context);
}
