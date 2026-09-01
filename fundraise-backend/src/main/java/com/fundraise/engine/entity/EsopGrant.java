package com.fundraise.engine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "esop_grants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsopGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String grantee;

    @Column(nullable = false)
    private Long shares;

    @Builder.Default
    private Boolean boardApproved = false;

    private LocalDate grantDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    private Document sourceDocument;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
