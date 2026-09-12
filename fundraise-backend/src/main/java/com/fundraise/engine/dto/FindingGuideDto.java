package com.fundraise.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class FindingGuideDto {
    private String ruleId;
    private String whyItMatters;
    private List<String> howToFix;
    private String whatGoodLooksLike;
    private String effortLevel;
    private String estimatedTime;
    private boolean needsAdvisor;
    private List<String> relatedCases;
}
