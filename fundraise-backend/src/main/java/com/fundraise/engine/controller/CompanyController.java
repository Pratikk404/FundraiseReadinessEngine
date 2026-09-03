package com.fundraise.engine.controller;

import com.fundraise.engine.dto.CompanyDto;
import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Companies", description = "Manage startups and their compliance profiles")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "List my companies", description = "Get all companies owned by the authenticated user.")
    public ResponseEntity<List<CompanyDto>> getMyCompanies(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(companyService.getCompaniesByUser(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company details", description = "Get full details of a specific company including documents and findings.")
    public ResponseEntity<Company> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PostMapping
    @Operation(summary = "Create company", description = "Register a new startup company profile.")
    public ResponseEntity<Company> createCompany(
            @RequestBody Company company,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(companyService.createCompany(company, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company", description = "Update company details (name, DPIIT status, etc).")
    public ResponseEntity<Company> updateCompany(
            @PathVariable UUID id,
            @RequestBody Company updates) {
        return ResponseEntity.ok(companyService.updateCompany(id, updates));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Company status summary", description = "Quick status overview: DPIIT status, document count, etc.")
    public ResponseEntity<Map<String, Object>> getCompanyStatus(@PathVariable UUID id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(Map.of(
                "id", company.getId(),
                "name", company.getName(),
                "dpiitStatus", company.getDpiitStatus(),
                "hasIncorporationDocs", !company.getDocuments().isEmpty()
        ));
    }
}
