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

class DilutionSumRuleTest {

    private DilutionSumRule rule;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        rule = new DilutionSumRule();
        testCompany = Company.builder()
                .id(java.util.UUID.randomUUID())
                .name("Test Corp")
                .dpiitStatus(Company.DpiitStatus.RECOGNIZED)
                .build();
    }

    @Nested
    @DisplayName("Equity Sum Verification")
    class EquitySumVerification {

        @Test
        @DisplayName("No findings when equity sums to exactly 100%")
        void noFindingsWhenSumIs100Percent() {
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

            assertTrue(findings.isEmpty(), "Should have no findings when equity sums to 100%");
        }

        @Test
        @DisplayName("CRITICAL finding when equity sums to less than 100%")
        void criticalFindingWhenSumLessThan100Percent() {
            // 65% + 20% + 9% = 94% (missing 6%)
            // Share class total = 6.5M + 2M + 1.5M = 10M
            // Issued = 6.5M + 2M + 0.9M = 9.4M
            // 9.4M / 10M = 94%
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 6_500_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L),
                            event(EquityEvent.InstrumentType.ESOP_POOL, 900_000L)
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            shareClass("Ordinary", 6_500_000L),
                            shareClass("Preferred_A", 2_000_000L),
                            shareClass("ESOP Pool", 1_500_000L)
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertFalse(findings.isEmpty(), "Should have findings for 94% sum");
            assertEquals(Finding.Severity.CRITICAL, findings.get(0).getSeverity());
            // The description uses %.2f%% format, so 94% becomes "94.00%"
            assertTrue(findings.get(0).getDescription().contains("94.00%"),
                    "Description should mention 94.00%");
        }

        @Test
        @DisplayName("CRITICAL finding when equity sums to more than 100%")
        void criticalFindingWhenSumMoreThan100Percent() {
            // Events: 70% + 25% + 10% = 10.5M issued
            // Share classes: 7M + 2M + 1M = 10M authorized
            // 10.5M / 10M = 105%
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 7_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_500_000L),
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

            assertFalse(findings.isEmpty(), "Should have findings for 105% sum");
            assertEquals(Finding.Severity.CRITICAL, findings.get(0).getSeverity());
            assertTrue(findings.get(0).getDescription().contains("105.00%"),
                    "Description should mention 105.00%");
        }

        @Test
        @DisplayName("No findings when no share class data available")
        void noFindingsWhenNoShareClassData() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 5_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L)
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of()) // No share class data
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings when share class data is unavailable");
        }

        @Test
        @DisplayName("No findings when share class total is zero")
        void noFindingsWhenShareClassTotalIsZero() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 5_000_000L)
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            shareClass("Ordinary", 0L)
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings when share class total is zero");
        }
    }

    @Nested
    @DisplayName("Informal ESOP Detection")
    class InformalEsopDetection {

        @Test
        @DisplayName("WARNING finding when informal ESOP grants exist")
        void warningFindingForInformalEsopGrants() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 7_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L),
                            event(EquityEvent.InstrumentType.ESOP_POOL, 1_000_000L)
                    ))
                    .esopGrants(List.of(
                            EsopGrant.builder()
                                    .grantee("CTO Candidate")
                                    .shares(300_000L)
                                    .boardApproved(false)
                                    .build()
                    ))
                    .shareClasses(List.of(
                            shareClass("Ordinary", 7_000_000L),
                            shareClass("Preferred_A", 2_000_000L),
                            shareClass("ESOP Pool", 1_000_000L)
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            // Should have the informal ESOP finding (WARNING)
            long warningCount = findings.stream()
                    .filter(f -> f.getSeverity() == Finding.Severity.WARNING)
                    .count();
            assertEquals(1, warningCount, "Should have exactly 1 WARNING for informal ESOP");
            assertTrue(findings.get(0).getDescription().contains("300,000"),
                    "Description should mention the share count");
        }

        @Test
        @DisplayName("No informal ESOP finding when all grants are board-approved")
        void noInformalEsopFindingWhenAllApproved() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 7_000_000L),
                            event(EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L),
                            event(EquityEvent.InstrumentType.ESOP_POOL, 1_000_000L)
                    ))
                    .esopGrants(List.of(
                            EsopGrant.builder()
                                    .grantee("Engineer")
                                    .shares(100_000L)
                                    .boardApproved(true)
                                    .build()
                    ))
                    .shareClasses(List.of(
                            shareClass("Ordinary", 7_000_000L),
                            shareClass("Preferred_A", 2_000_000L),
                            shareClass("ESOP Pool", 1_000_000L)
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings when all ESOP grants are board-approved");
        }
    }

    @Test
    @DisplayName("Rule metadata is correct")
    void ruleMetadata() {
        assertEquals("DILUTION_SUM_100", rule.getRuleId());
        assertEquals("CAP_TABLE", rule.getCategory());
        assertEquals(Finding.Severity.CRITICAL, rule.getSeverity());
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
