package com.fundraise.engine.rules;

import com.fundraise.engine.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RulesEngineTest {

    private RulesEngine engine;

    @BeforeEach
    void setUp() {
        // Create engine with all 5 rules
        List<ComplianceRule> rules = List.of(
                new DilutionSumRule(),
                new DpiitRecognitionRule(),
                new EsopConsistencyRule(),
                new ShareClassConsistencyRule(),
                new ValuationConsistencyRule()
        );
        engine = new RulesEngine(rules);
    }

    @Test
    @DisplayName("Engine discovers all 5 rules")
    void discoversAllRules() {
        assertEquals(5, engine.getRules().size());
    }

    @Test
    @DisplayName("Clean company produces no findings")
    void cleanCompanyNoFindings() {
        Company company = Company.builder()
                .id(java.util.UUID.randomUUID())
                .name("CleanTech Solutions")
                .dpiitStatus(Company.DpiitStatus.RECOGNIZED)
                .dpiitRecognitionDate(LocalDate.of(2023, 4, 1))
                .build();

        ComplianceContext context = ComplianceContext.builder()
                .company(company)
                .equityEvents(List.of(
                        equityEvent(company, "Pre-Seed", EquityEvent.InstrumentType.COMMON, 7_000_000L, new BigDecimal("1.00")),
                        equityEvent(company, "Seed", EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L, new BigDecimal("10.00")),
                        equityEvent(company, "ESOP", EquityEvent.InstrumentType.ESOP_POOL, 1_000_000L, new BigDecimal("5.00"))
                ))
                .esopGrants(List.of(
                        EsopGrant.builder().company(company).grantee("Engineer").shares(100_000L).boardApproved(true).build()
                ))
                .shareClasses(List.of(
                        shareClass(company, "Ordinary", 7_000_000L),
                        shareClass(company, "Preferred_A", 2_000_000L),
                        shareClass(company, "ESOP Pool", 1_000_000L)
                ))
                .documents(List.of())
                .build();

        List<Finding> findings = engine.evaluateAll(context);

        assertTrue(findings.isEmpty(),
                "Clean company should have no findings, but got: " + findings);
    }

    @Test
    @DisplayName("Company with multiple issues produces multiple findings")
    void companyWithIssuesProducesFindings() {
        Company company = Company.builder()
                .id(java.util.UUID.randomUUID())
                .name("Problem Corp")
                .dpiitStatus(Company.DpiitStatus.NOT_RECOGNIZED)
                .build();

        ComplianceContext context = ComplianceContext.builder()
                .company(company)
                .equityEvents(List.of(
                        // Dilution sums to 94% (65% + 20% + 9%)
                        equityEvent(company, "Pre-Seed", EquityEvent.InstrumentType.COMMON, 6_500_000L, BigDecimal.ONE),
                        equityEvent(company, "Seed", EquityEvent.InstrumentType.PREFERRED_A, 2_000_000L, new BigDecimal("10.00")),
                        equityEvent(company, "ESOP", EquityEvent.InstrumentType.ESOP_POOL, 900_000L, new BigDecimal("5.00"))
                ))
                .esopGrants(List.of(
                        // Informal ESOP
                        EsopGrant.builder().company(company).grantee("CTO").shares(300_000L).boardApproved(false).build()
                ))
                .shareClasses(List.of(
                        shareClass(company, "Ordinary", 6_500_000L),
                        shareClass(company, "Preferred_A", 2_000_000L),
                        shareClass(company, "ESOP Pool", 1_500_000L)
                ))
                .documents(List.of())
                .build();

        List<Finding> findings = engine.evaluateAll(context);

        assertFalse(findings.isEmpty(), "Problem company should have findings");

        // Should have DPIIT finding (NOT_RECOGNIZED)
        boolean hasDpiitFinding = findings.stream()
                .anyMatch(f -> f.getRuleId().equals("DPIIT_RECOGNITION"));
        assertTrue(hasDpiitFinding, "Should have DPIIT finding");

        // Should have dilution finding (94% != 100%)
        boolean hasDilutionFinding = findings.stream()
                .anyMatch(f -> f.getRuleId().equals("DILUTION_SUM_100"));
        assertTrue(hasDilutionFinding, "Should have dilution finding");

        // Should have ESOP finding (informal grant)
        boolean hasEsopFinding = findings.stream()
                .anyMatch(f -> f.getRuleId().equals("ESOP_CONSISTENCY"));
        assertTrue(hasEsopFinding, "Should have ESOP finding");
    }

    @Test
    @DisplayName("Engine handles empty context gracefully")
    void handlesEmptyContext() {
        Company company = Company.builder()
                .id(java.util.UUID.randomUUID())
                .name("Empty Corp")
                .dpiitStatus(Company.DpiitStatus.UNKNOWN)
                .build();

        ComplianceContext context = ComplianceContext.builder()
                .company(company)
                .equityEvents(List.of())
                .esopGrants(List.of())
                .shareClasses(List.of())
                .documents(List.of())
                .build();

        List<Finding> findings = engine.evaluateAll(context);

        // Should still get DPIIT UNKNOWN warning
        assertFalse(findings.isEmpty(), "Should have at least DPIIT finding for UNKNOWN");
    }

    private EquityEvent equityEvent(Company company, String round,
                                     EquityEvent.InstrumentType type,
                                     long shares, BigDecimal price) {
        return EquityEvent.builder()
                .company(company)
                .roundName(round)
                .instrumentType(type)
                .sharesIssued(shares)
                .pricePerShare(price)
                .eventDate(LocalDate.now())
                .build();
    }

    private ShareClass shareClass(Company company, String name, long totalShares) {
        return ShareClass.builder()
                .company(company)
                .className(name)
                .totalShares(totalShares)
                .build();
    }
}
