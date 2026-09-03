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

class EsopConsistencyRuleTest {

    private EsopConsistencyRule rule;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        rule = new EsopConsistencyRule();
        testCompany = Company.builder()
                .id(java.util.UUID.randomUUID())
                .name("Test Corp")
                .dpiitStatus(Company.DpiitStatus.RECOGNIZED)
                .build();
    }

    @Nested
    @DisplayName("Informal Grant Detection")
    class InformalGrantDetection {

        @Test
        @DisplayName("CRITICAL finding when ESOP grants lack board approval")
        void criticalFindingForInformalGrants() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.ESOP_POOL, 1_000_000L)
                    ))
                    .esopGrants(List.of(
                            EsopGrant.builder()
                                    .grantee("Marketing Head")
                                    .shares(200_000L)
                                    .boardApproved(false)
                                    .grantDate(LocalDate.of(2024, 1, 1))
                                    .build(),
                            EsopGrant.builder()
                                    .grantee("Senior Engineer")
                                    .shares(150_000L)
                                    .boardApproved(false)
                                    .grantDate(LocalDate.of(2024, 3, 15))
                                    .build()
                    ))
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertFalse(findings.isEmpty(), "Should have findings for informal grants");
            assertEquals(Finding.Severity.CRITICAL, findings.get(0).getSeverity());
            assertTrue(findings.get(0).getDescription().contains("2"),
                    "Should mention 2 grants");
            assertTrue(findings.get(0).getDescription().contains("350,000"),
                    "Should mention total shares");
            assertTrue(findings.get(0).getDescription().contains("Marketing Head"),
                    "Should mention grantee names");
        }

        @Test
        @DisplayName("No findings when all grants are board-approved")
        void noFindingsWhenAllApproved() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.ESOP_POOL, 1_000_000L)
                    ))
                    .esopGrants(List.of(
                            EsopGrant.builder()
                                    .grantee("Engineer")
                                    .shares(100_000L)
                                    .boardApproved(true)
                                    .grantDate(LocalDate.of(2024, 1, 1))
                                    .build()
                    ))
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings when all grants are approved");
        }
    }

    @Nested
    @DisplayName("ESOP Pool Overflow")
    class EsopPoolOverflow {

        @Test
        @DisplayName("WARNING when grants exceed declared ESOP pool")
        void warningWhenGrantsExceedPool() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.ESOP_POOL, 500_000L)
                    ))
                    .esopGrants(List.of(
                            EsopGrant.builder()
                                    .grantee("Person A")
                                    .shares(300_000L)
                                    .boardApproved(true)
                                    .build(),
                            EsopGrant.builder()
                                    .grantee("Person B")
                                    .shares(250_000L)
                                    .boardApproved(true)
                                    .build()
                    ))
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            // Should find the pool overflow warning
            long poolWarnings = findings.stream()
                    .filter(f -> f.getSeverity() == Finding.Severity.WARNING)
                    .filter(f -> f.getDescription().contains("exceed"))
                    .count();
            assertEquals(1, poolWarnings, "Should have 1 pool overflow warning");
        }

        @Test
        @DisplayName("No pool overflow when grants are within pool")
        void noPoolOverflowWhenWithinPool() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.ESOP_POOL, 1_000_000L)
                    ))
                    .esopGrants(List.of(
                            EsopGrant.builder()
                                    .grantee("Engineer")
                                    .shares(400_000L)
                                    .boardApproved(true)
                                    .build()
                    ))
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings when grants are within pool");
        }

        @Test
        @DisplayName("No pool check when no ESOP pool in cap table")
        void noPoolCheckWhenNoPoolDeclared() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            event(EquityEvent.InstrumentType.COMMON, 10_000_000L)
                    ))
                    .esopGrants(List.of(
                            EsopGrant.builder()
                                    .grantee("Engineer")
                                    .shares(500_000L)
                                    .boardApproved(true)
                                    .build()
                    ))
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings when no ESOP pool is declared");
        }
    }

    @Test
    @DisplayName("Rule metadata is correct")
    void ruleMetadata() {
        assertEquals("ESOP_CONSISTENCY", rule.getRuleId());
        assertEquals("ESOP", rule.getCategory());
        assertEquals(Finding.Severity.CRITICAL, rule.getSeverity());
        assertNotNull(rule.getDescription());
    }

    private EquityEvent event(EquityEvent.InstrumentType type, long shares) {
        return EquityEvent.builder()
                .company(testCompany)
                .roundName("Test Round")
                .instrumentType(type)
                .sharesIssued(shares)
                .pricePerShare(BigDecimal.ZERO)
                .eventDate(LocalDate.now())
                .build();
    }
}
