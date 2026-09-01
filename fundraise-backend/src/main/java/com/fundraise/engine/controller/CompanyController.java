package com.fundraise.engine.controller;

import com.fundraise.engine.dto.CompanyDto;
import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.service.CompanyService;
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
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyDto>> getMyCompanies(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(companyService.getCompaniesByUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(
            @RequestBody Company company,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(companyService.createCompany(company, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(
            @PathVariable UUID id,
            @RequestBody Company updates) {
        return ResponseEntity.ok(companyService.updateCompany(id, updates));
    }

    @GetMapping("/{id}/status")
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
