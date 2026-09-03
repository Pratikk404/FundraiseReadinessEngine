package com.fundraise.engine.rules;

import com.fundraise.engine.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShareClassConsistencyRuleTest {

    private ShareClassConsistencyRule rule;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        rule = new ShareClassConsistencyRule();
        testCompany = Company.builder()
                .id(java.util.UUID.randomUUID())
                .name("Test Corp")
                .dpiitStatus(Company.DpiitStatus.RECOGNIZED)
                .build();
    }

    @Nested
    @DisplayName("Total Share Consistency")
    class TotalShareConsistency {

        @Test
        @DisplayName("No findings when share class totals match equity events")
        void noFindingsWhenTotalsMatch() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 7_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L),
                            event(EquityEvent.InstrumentType.ESOP_POOL, 1_000_000L)
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            shareClass("Ordinary", 7_000_000L),
                            shareClass("Preferred_A", 2_000_000L),
                            shareClass("ESOP Pool", 1_000_000L)
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(), "Should have no findings when totals match");
        }

        @Test
        @DisplayName("WARNING when share class totals differ from equity events")
        void warningWhenTotalsMismatch() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 6_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_B, 1_500_000L),
                            event(EquityEvent.InstrumentType.ESOP_POOL, 500_000L)
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            shareClass("Ordinary", 6_000_000L),
                            shareClass("Preferred_A", 2_000_000L),
                            // Preferred_B exists in events but not in share classes
                            shareClass("ESOP Pool", 500_000L)
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            // Should find the total mismatch warning
            long mismatchWarnings = findings.stream()
                    .filter(f -> f.getDescription().contains("does not match"))
                    .count();
            assertEquals(1, mismatchWarnings, "Should have 1 total mismatch warning");
        }
    }

    @Nested
    @DisplayName("Unused Share Classes")
    class UnusedShareClasses {

        @Test
        @DisplayName("INFO finding for share classes declared but not used in events")
        void infoFindingForUnusedClasses() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 7_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L)
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            shareClass("Ordinary", 7_000_000L),
                            shareClass("Preferred_A", 2_000_000L),
                            shareClass("Preferred_B", 1_000_000L) // Declared but not used
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            long unusedFindings = findings.stream()
                    .filter(f -> f.getDescription().contains("declared in documents but has no corresponding"))
                    .count();
            assertEquals(1, unusedFindings, "Should find 1 unused share class");
            assertTrue(findings.stream()
                    .anyMatch(f -> f.getDescription().contains("Preferred_B")),
                    "Should mention Preferred_B");
        }

        @Test
        @DisplayName("No findings when all declared classes are used")
        void noFindingsWhenAllClassesUsed() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 7_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 3_000_000L)
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            shareClass("Ordinary", 7_000_000L),
                            shareClass("Preferred_A", 3_000_000L)
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings when all classes are used");
        }
    }

    @Test
    @DisplayName("Rule metadata is correct")
    void ruleMetadata() {
        assertEquals("SHARE_CLASS_CONSISTENCY", rule.getRuleId());
        assertEquals("SHARE_STRUCTURE", rule.getCategory());
        assertEquals(Finding.Severity.WARNING, rule.getSeverity());
        assertNotNull(rule.getDescription());
    }

    private EquityEvent event(EquityEvent.InstrumentType type, long shares) {
        return EquityEvent.builder()
                .company(testCompany)
                .roundName("Test Round")
                .instrumentType(type)
                .sharesIssued(shares)
                .pricePerShare(BigDecimal.ONE)
                .eventDate(LocalDate.now())
                .build();
    }

    private ShareClass shareClass(String name, long totalShares) {
        return ShareClass.builder()
                .company(testCompany)
                .className(name)
                .totalShares(totalShares)
                .build();
    }
}
