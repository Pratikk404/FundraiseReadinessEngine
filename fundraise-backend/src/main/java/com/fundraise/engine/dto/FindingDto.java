package com.fundraise.engine.dto;

import com.fundraise.engine.entity.Finding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FindingDto {
    private Long id;
    private String ruleId;
    private String category;
    private Finding.Severity severity;
    private String description;
    private boolean resolved;
    private String sourceDocument;

    public static FindingDto fromEntity(Finding finding) {
        return FindingDto.builder()
                .id(finding.getId())
                .ruleId(finding.getRuleId())
                .category(finding.getCategory())
                .severity(finding.getSeverity())
                .description(finding.getDescription())
                .resolved(finding.isResolved())
                .sourceDocument(finding.getSourceDocument() != null
                        ? finding.getSourceDocument().getFilename()
                        : null)
                .build();
    }
}
