package com.fundraise.engine.dto;

import com.fundraise.engine.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class CompanyDto {
    private UUID id;
    private String name;
    private LocalDate incorporationDate;
    private Company.DpiitStatus dpiitStatus;
    private LocalDate dpiitRecognitionDate;
    private int totalFindings;
    private int criticalFindings;
    private int warningFindings;

    public static CompanyDto fromEntity(Company company, int critical, int warning) {
        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .incorporationDate(company.getIncorporationDate())
                .dpiitStatus(company.getDpiitStatus())
                .dpiitRecognitionDate(company.getDpiitRecognitionDate())
                .totalFindings(critical + warning)
                .criticalFindings(critical)
                .warningFindings(warning)
                .build();
    }
}
