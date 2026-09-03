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

class ValuationConsistencyRuleTest {

    private ValuationConsistencyRule rule;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        rule = new ValuationConsistencyRule();
        testCompany = Company.builder()
                .id(java.util.UUID.randomUUID())
                .name("Test Corp")
                .dpiitStatus(Company.DpiitStatus.RECOGNIZED)
                .build();
    }

    @Nested
    @DisplayName("Zero Price Detection")
    class ZeroPriceDetection {

        @Test
        @DisplayName("CRITICAL finding when price per share is zero")
        void criticalFindingForZeroPrice() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            EquityEvent.builder()
                                    .company(testCompany)
                                    .roundName("Seed Round")
                                    .instrumentType(EquityEvent.InstrumentType.PREFERRED_A)
                                    .sharesIssued(2_000_000L)
                                    .pricePerShare(BigDecimal.ZERO)
                                    .eventDate(LocalDate.of(2024, 1, 1))
                                    .build()
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            ShareClass.builder()
                                    .company(testCompany)
                                    .className("Ordinary")
                                    .totalShares(7_000_000L)
                                    .build()
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertFalse(findings.isEmpty(), "Should have findings for zero price");
            assertEquals(Finding.Severity.CRITICAL, findings.get(0).getSeverity());
            assertTrue(findings.get(0).getDescription().contains("zero or negative"),
                    "Description should mention zero/negative price");
        }

        @Test
        @DisplayName("CRITICAL finding when price per share is negative")
        void criticalFindingForNegativePrice() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            EquityEvent.builder()
                                    .company(testCompany)
                                    .roundName("Pre-Seed")
                                    .instrumentType(EquityEvent.InstrumentType.COMMON)
                                    .sharesIssued(5_000_000L)
                                    .pricePerShare(new BigDecimal("-5.00"))
                                    .eventDate(LocalDate.of(2023, 6, 1))
                                    .build()
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertFalse(findings.isEmpty(), "Should have findings for negative price");
            assertEquals(Finding.Severity.CRITICAL, findings.get(0).getSeverity());
        }
    }

    @Nested
    @DisplayName("No Findings for Valid Data")
    class ValidData {

        @Test
        @DisplayName("No findings when price per share is reasonable")
        void noFindingsForReasonablePrice() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            EquityEvent.builder()
                                    .company(testCompany)
                                    .roundName("Seed")
                                    .instrumentType(EquityEvent.InstrumentType.COMMON)
                                    .sharesIssued(7_000_000L)
                                    .pricePerShare(new BigDecimal("1.00"))
                                    .eventDate(LocalDate.of(2023, 3, 1))
                                    .build(),
                            EquityEvent.builder()
                                    .company(testCompany)
                                    .roundName("Seed")
                                    .instrumentType(EquityEvent.InstrumentType.PREFERRED_A)
                                    .sharesIssued(2_000_000L)
                                    .pricePerShare(new BigDecimal("10.00"))
                                    .eventDate(LocalDate.of(2024, 1, 1))
                                    .build()
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of(
                            ShareClass.builder()
                                    .company(testCompany)
                                    .className("Ordinary")
                                    .totalShares(7_000_000L)
                                    .build(),
                            ShareClass.builder()
                                    .company(testCompany)
                                    .className("Preferred_A")
                                    .totalShares(2_000_000L)
                                    .build()
                    ))
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(), "Should have no findings for reasonable prices");
        }

        @Test
        @DisplayName("No findings when events have no price per share")
        void noFindingsWhenNoPrice() {
            ComplianceContext context = ComplianceContext.builder()
                    .company(testCompany)
                    .equityEvents(List.of(
                            EquityEvent.builder()
                                    .company(testCompany)
                                    .roundName("Pre-Seed")
                                    .instrumentType(EquityEvent.InstrumentType.COMMON)
                                    .sharesIssued(10_000_000L)
                                    .pricePerShare(null)
                                    .eventDate(LocalDate.of(2023, 1, 1))
                                    .build()
                    ))
                    .esopGrants(List.of())
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(), "Should have no findings when price is null");
        }
    }

    @Test
    @DisplayName("Rule metadata is correct")
    void ruleMetadata() {
        assertEquals("VALUATION_CONSISTENCY", rule.getRuleId());
        assertEquals("VALUATION", rule.getCategory());
        assertEquals(Finding.Severity.WARNING, rule.getSeverity());
        assertNotNull(rule.getDescription());
    }
}
