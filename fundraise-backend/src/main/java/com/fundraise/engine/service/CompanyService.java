package com.fundraise.engine.service;

import com.fundraise.engine.dto.CompanyDto;
import com.fundraise.engine.entity.Company;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.repository.CompanyRepository;
import com.fundraise.engine.repository.FindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final FindingRepository findingRepository;

    public List<CompanyDto> getCompaniesByUser(User user) {
        return companyRepository.findByOwner(user).stream()
                .map(company -> {
                    int critical = (int) findingRepository.findByCompanyId(company.getId()).stream()
                            .filter(f -> f.getSeverity() == com.fundraise.engine.entity.Finding.Severity.CRITICAL && !f.isResolved())
                            .count();
                    int warning = (int) findingRepository.findByCompanyId(company.getId()).stream()
                            .filter(f -> f.getSeverity() == com.fundraise.engine.entity.Finding.Severity.WARNING && !f.isResolved())
                            .count();
                    return CompanyDto.fromEntity(company, critical, warning);
                })
                .toList();
    }

    public Company getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));
    }

    public Company createCompany(Company company, User owner) {
        company.setOwner(owner);
        return companyRepository.save(company);
    }

    public Company updateCompany(UUID id, Company updates) {
        Company company = getCompanyById(id);
        company.setName(updates.getName());
        company.setIncorporationDate(updates.getIncorporationDate());
        company.setDpiitStatus(updates.getDpiitStatus());
        company.setDpiitRecognitionDate(updates.getDpiitRecognitionDate());
        return companyRepository.save(company);
    }
}
