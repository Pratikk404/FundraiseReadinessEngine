package com.fundraise.engine.rules;

import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.Finding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DpiitRecognitionRuleTest {

    private DpiitRecognitionRule rule;

    @BeforeEach
    void setUp() {
        rule = new DpiitRecognitionRule();
    }

    @Nested
    @DisplayName("RECOGNIZED Status")
    class RecognizedStatus {

        @Test
        @DisplayName("No findings when company has recent DPIIT recognition")
        void noFindingsWhenRecentlyRecognized() {
            Company company = Company.builder()
                    .id(java.util.UUID.randomUUID())
                    .name("CleanTech Solutions")
                    .dpiitStatus(Company.DpiitStatus.RECOGNIZED)
                    .dpiitRecognitionDate(LocalDate.of(2023, 4, 1))
                    .build();

            ComplianceContext context = ComplianceContext.builder()
                    .company(company)
                    .equityEvents(List.of())
                    .esopGrants(List.of())
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertTrue(findings.isEmpty(),
                    "Should have no findings for recently recognized company");
        }

        @Test
        @DisplayName("WARNING when recognition is older than 10 years")
        void warningWhenRecognitionLapsed() {
            Company company = Company.builder()
                    .id(java.util.UUID.randomUUID())
                    .name("Old Startup")
                    .dpiitStatus(Company.DpiitStatus.RECOGNIZED)
                    .dpiitRecognitionDate(LocalDate.of(2015, 1, 1)) // 11+ years ago
                    .build();

            ComplianceContext context = ComplianceContext.builder()
                    .company(company)
                    .equityEvents(List.of())
                    .esopGrants(List.of())
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertFalse(findings.isEmpty(), "Should have findings for lapsed recognition");
            assertEquals(Finding.Severity.WARNING, findings.get(0).getSeverity());
            assertTrue(findings.get(0).getDescription().contains("10 years"),
                    "Description should mention 10-year validity");
        }
    }

    @Nested
    @DisplayName("NOT_RECOGNIZED Status")
    class NotRecognizedStatus {

        @Test
        @DisplayName("CRITICAL finding when company has no DPIIT recognition")
        void criticalFindingWhenNotRecognized() {
            Company company = Company.builder()
                    .id(java.util.UUID.randomUUID())
                    .name("GreenEnergy India")
                    .dpiitStatus(Company.DpiitStatus.NOT_RECOGNIZED)
                    .build();

            ComplianceContext context = ComplianceContext.builder()
                    .company(company)
                    .equityEvents(List.of())
                    .esopGrants(List.of())
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertFalse(findings.isEmpty(), "Should have findings for missing DPIIT");
            assertEquals(Finding.Severity.CRITICAL, findings.get(0).getSeverity());
            assertTrue(findings.get(0).getDescription().contains("Section 56(2)(viib)"),
                    "Description should mention angel tax section");
        }
    }

    @Nested
    @DisplayName("UNKNOWN Status")
    class UnknownStatus {

        @Test
        @DisplayName("WARNING finding when DPIIT status is unknown")
        void warningFindingWhenStatusUnknown() {
            Company company = Company.builder()
                    .id(java.util.UUID.randomUUID())
                    .name("Mystery Corp")
                    .dpiitStatus(Company.DpiitStatus.UNKNOWN)
                    .build();

            ComplianceContext context = ComplianceContext.builder()
                    .company(company)
                    .equityEvents(List.of())
                    .esopGrants(List.of())
                    .shareClasses(List.of())
                    .documents(List.of())
                    .build();

            List<Finding> findings = rule.evaluate(context);

            assertFalse(findings.isEmpty(), "Should have findings for unknown status");
            assertEquals(Finding.Severity.WARNING, findings.get(0).getSeverity());
            assertTrue(findings.get(0).getDescription().contains("UNKNOWN"),
                    "Description should mention UNKNOWN status");
            assertTrue(findings.get(0).getDescription().contains("startupindia.gov.in"),
                    "Description should reference DPIIT portal");
        }
    }

    @Test
    @DisplayName("Rule metadata is correct")
    void ruleMetadata() {
        assertEquals("DPIIT_RECOGNITION", rule.getRuleId());
        assertEquals("DPIIT", rule.getCategory());
        assertEquals(Finding.Severity.CRITICAL, rule.getSeverity());
        assertNotNull(rule.getDescription());
    }
}
