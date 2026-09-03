package com.fundraise.engine.integration;

import com.fundraise.engine.dto.AuthRequest;
import com.fundraise.engine.dto.AuthResponse;
import com.fundraise.engine.dto.LoginRequest;
import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.repository.*;
import com.fundraise.engine.service.AuthService;
import com.fundraise.engine.service.ComplianceService;
import com.fundraise.engine.service.CompanyService;
import com.fundraise.engine.service.GapReportService;
import com.fundraise.engine.service.PdfExportService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test covering the full user flow:
 * Register → Login → Create Company → Run Check → Get Findings → Get Gap Report
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ComplianceService complianceService;

    @Autowired
    private GapReportService gapReportService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private static UUID companyId;
    private static String authToken;

    @Test
    @Order(1)
    @DisplayName("Register a new user")
    void registerUser() {
        AuthRequest request = new AuthRequest();
        request.setName("Test Founder");
        request.setEmail("founder-" + System.currentTimeMillis() + "@test.com");
        request.setPassword("password123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Test Founder", response.getName());
        assertEquals("FOUNDER", response.getRole());

        authToken = response.getToken();
    }

    @Test
    @Order(2)
    @DisplayName("Login with registered user")
    void loginUser() {
        // Use the test seeded user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("pratik@test.com");
        loginRequest.setPassword("test123");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        authToken = response.getToken();
    }

    @Test
    @Order(3)
    @DisplayName("Create a company with issues")
    void createCompany() {
        User user = userRepository.findByEmail("pratik@test.com")
                .orElseThrow();

        Company company = Company.builder()
                .name("Integration Test Corp " + System.currentTimeMillis())
                .incorporationDate(LocalDate.of(2023, 6, 1))
                .dpiitStatus(Company.DpiitStatus.NOT_RECOGNIZED)
                .build();

        Company created = companyService.createCompany(company, user);
        companyId = created.getId();

        assertNotNull(companyId);
        assertEquals("NOT_RECOGNIZED", created.getDpiitStatus().name());
    }

    @Test
    @Order(4)
    @DisplayName("Run compliance check on company")
    void runComplianceCheck() {
        assertNotNull(companyId, "Company must be created first");

        Map<String, Object> result = complianceService.runComplianceCheck(companyId);

        assertNotNull(result);
        assertEquals(companyId, result.get("companyId"));
        assertTrue(((Number) result.get("totalFindings")).longValue() > 0, "Should have findings for a problematic company");
        assertTrue(((Number) result.get("criticalCount")).longValue() > 0, "Should have critical findings");
    }

    @Test
    @Order(5)
    @DisplayName("Get findings for company")
    void getFindings() {
        assertNotNull(companyId);

        var findings = complianceService.getFindings(companyId, null);

        assertFalse(findings.isEmpty(), "Should have findings");
        // The company has NOT_RECOGNIZED DPIIT status, so DPIIT rule should fire
        assertTrue(findings.stream().anyMatch(f ->
                f.getRuleId().equals("DPIIT_RECOGNITION")), "Should have DPIIT finding");
    }

    @Test
    @Order(6)
    @DisplayName("Get readiness scores")
    void getScores() {
        assertNotNull(companyId);

        var scores = complianceService.getScores(companyId);

        assertFalse(scores.isEmpty(), "Should have scores");
        assertTrue(scores.stream().anyMatch(s ->
                s.getCategory().equals("DPIIT")), "Should have DPIIT score");
    }

    @Test
    @Order(7)
    @DisplayName("Get score history")
    void getScoreHistory() {
        assertNotNull(companyId);

        var history = complianceService.getScoreHistory(companyId);

        assertFalse(history.isEmpty(), "Should have score history");
        assertNotNull(history.get(0).get("overallScore"));
        assertNotNull(history.get(0).get("categoryScores"));
    }

    @Test
    @Order(8)
    @DisplayName("Generate gap report")
    void generateGapReport() {
        assertNotNull(companyId);

        Map<String, Object> report = gapReportService.generateGapReport(companyId);

        assertNotNull(report);
        assertEquals(companyId, report.get("companyId"));
        assertNotNull(report.get("narrative"));
        assertNotNull(report.get("readiness"));
        assertNotNull(report.get("priorityActions"));
        assertNotNull(report.get("categoryBreakdown"));
    }

    @Test
    @Order(9)
    @DisplayName("Export PDF report")
    void exportPdfReport() {
        assertNotNull(companyId);

        String html = pdfExportService.generateReport(companyId);

        assertNotNull(html);
        assertTrue(html.contains("<!DOCTYPE html>"), "Should be valid HTML");
        assertTrue(html.contains("Compliance Gap Report"), "Should contain report title");
        assertTrue(html.contains("FundraiseReady"), "Should contain brand");
    }

    @Test
    @Order(10)
    @DisplayName("Resolve a finding and re-check")
    void resolveFindingAndRecheck() {
        assertNotNull(companyId);

        // Get findings
        var findings = complianceService.getFindings(companyId, null);
        assertFalse(findings.isEmpty());

        // Resolve first finding
        Long findingId = findings.get(0).getId();
        complianceService.resolveFinding(findingId);

        // Verify it's resolved
        var updatedFindings = complianceService.getFindings(companyId, null);
        assertTrue(updatedFindings.stream().anyMatch(f ->
                f.getId().equals(findingId) && f.isResolved()));
    }
}
