package com.fundraise.engine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "finding_guides")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindingGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String ruleId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String whyItMatters;

    @ElementCollection
    @CollectionTable(name = "finding_guide_fix_steps", joinColumns = @JoinColumn(name = "guide_id"))
    @Column(name = "step")
    @OrderColumn(name = "step_index")
    private List<String> howToFix;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String whatGoodLooksLike;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EffortLevel effortLevel;

    private String estimatedTime;

    @Builder.Default
    private boolean needsAdvisor = false;

    @ElementCollection
    @CollectionTable(name = "finding_guide_related_cases", joinColumns = @JoinColumn(name = "guide_id"))
    @Column(name = "related_case")
    private List<String> relatedCases;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum EffortLevel {
        QUICK_FIX,
        MODERATE,
        NEEDS_ADVISOR
    }
}
